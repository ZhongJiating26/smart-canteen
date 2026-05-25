package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.common.util.SnowflakeIdUtil;
import cn.zhongjiating.canteen.order.dto.CreateOrderItemRequest;
import cn.zhongjiating.canteen.order.dto.CreateOrderRequest;
import cn.zhongjiating.canteen.order.dto.InventoryItemRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationResponse;
import cn.zhongjiating.canteen.order.dto.MenuItemSnapshotResponse;
import cn.zhongjiating.canteen.order.dto.MenuSnapshotRequest;
import cn.zhongjiating.canteen.order.dto.MenuSnapshotResponse;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderConfig;
import cn.zhongjiating.canteen.order.entity.OrderItem;
import cn.zhongjiating.canteen.order.feign.MenuFeignClient;
import cn.zhongjiating.canteen.order.feign.RemoteResultErrors;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import cn.zhongjiating.canteen.order.mapper.OrderItemMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mq.OrderTimeoutMessagePublisher;
import cn.zhongjiating.canteen.order.service.OrderCreateService;
import cn.zhongjiating.canteen.order.service.OrderIdempotencyService;
import cn.zhongjiating.canteen.order.vo.CreateOrderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderCreateServiceImpl implements OrderCreateService {

    private static final int DEFAULT_PAY_TIMEOUT_MINUTES = 10;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderConfigMapper orderConfigMapper;
    private final MenuFeignClient menuFeignClient;
    private final OrderIdempotencyService orderIdempotencyService;
    private final OrderTimeoutMessagePublisher timeoutMessagePublisher;
    private final ObjectMapper objectMapper;

    public OrderCreateServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                                  OrderConfigMapper orderConfigMapper, MenuFeignClient menuFeignClient,
                                  OrderIdempotencyService orderIdempotencyService,
                                  OrderTimeoutMessagePublisher timeoutMessagePublisher,
                                  ObjectMapper objectMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderConfigMapper = orderConfigMapper;
        this.menuFeignClient = menuFeignClient;
        this.orderIdempotencyService = orderIdempotencyService;
        this.timeoutMessagePublisher = timeoutMessagePublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    @GlobalTransactional(name = "create-order-reserve-inventory", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public CreateOrderVO create(String idempotentKey, CreateOrderRequest request) {
        UserContext user = requireUser();
        if (idempotentKey == null || idempotentKey.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "幂等 Key 不能为空");
        }
        String requestHash = requestHash(request);
        Long existingOrderId = orderIdempotencyService.findExistingOrderId(user.getUserId(),
                idempotentKey.trim(), requestHash);
        if (existingOrderId != null) {
            return CreateOrderVO.from(requireExistingOrder(existingOrderId), payTimeoutMinutes());
        }

        List<InventoryItemRequest> inventoryItems = request.items().stream()
                .map(CreateOrderItemRequest::toInventoryItem)
                .toList();
        MenuSnapshotResponse snapshot = snapshot(request.merchantId(), inventoryItems);
        Map<Long, MenuItemSnapshotResponse> snapshotsByItemId = snapshot.items().stream()
                .collect(Collectors.toMap(MenuItemSnapshotResponse::dailyMenuItemId, Function.identity()));
        Order order = buildOrder(user.getUserId(), request, snapshotsByItemId);
        orderMapper.insert(order);
        insertItems(order.getId(), request.items(), snapshotsByItemId);
        reserveInventory(order.getId(), inventoryItems);
        orderIdempotencyService.saveAfterCommit(user.getUserId(), idempotentKey.trim(), requestHash, order.getId());
        timeoutMessagePublisher.publishPayTimeout(order);
        return CreateOrderVO.from(order, payTimeoutMinutes());
    }

    private MenuSnapshotResponse snapshot(Long merchantId, List<InventoryItemRequest> inventoryItems) {
        Result<MenuSnapshotResponse> result = menuFeignClient.snapshot(new MenuSnapshotRequest(merchantId,
                inventoryItems));
        if (result == null || result.getCode() == null || result.getCode() != 200 || result.getData() == null) {
            throw remoteFailure(result, "菜单快照查询失败");
        }
        MenuSnapshotResponse snapshot = result.getData();
        if (!merchantId.equals(snapshot.merchantId()) || snapshot.items() == null
                || snapshot.items().size() != inventoryItems.stream()
                .map(InventoryItemRequest::dailyMenuItemId).collect(Collectors.toSet()).size()) {
            throw new BusinessException(ErrorCode.DISH_NOT_AVAILABLE);
        }
        return snapshot;
    }

    private Order buildOrder(Long userId, CreateOrderRequest request,
                             Map<Long, MenuItemSnapshotResponse> snapshotsByItemId) {
        Order order = new Order();
        long orderId = SnowflakeIdUtil.nextId();
        order.setId(orderId);
        order.setOrderNo(String.valueOf(orderId));
        order.setUserId(userId);
        order.setMerchantId(request.merchantId());
        order.setStatus(OrderStatus.PENDING_PAY.getCode());
        order.setRemark(trimToNull(request.remark()));
        order.setCreateTime(LocalDateTime.now());
        order.setTotalAmount(totalAmount(request.items(), snapshotsByItemId));
        return order;
    }

    private void insertItems(Long orderId, List<CreateOrderItemRequest> items,
                             Map<Long, MenuItemSnapshotResponse> snapshotsByItemId) {
        for (CreateOrderItemRequest requestItem : items) {
            MenuItemSnapshotResponse snapshot = requireSnapshot(requestItem.dailyMenuItemId(), snapshotsByItemId);
            OrderItem item = new OrderItem();
            item.setId(SnowflakeIdUtil.nextId());
            item.setOrderId(orderId);
            item.setDailyMenuItemId(snapshot.dailyMenuItemId());
            item.setDishId(snapshot.dishId());
            item.setDishName(snapshot.dishName());
            item.setDishImage(snapshot.dishImage());
            item.setUnitPrice(snapshot.unitPrice());
            item.setQuantity(requestItem.quantity());
            item.setSubtotal(snapshot.unitPrice().multiply(BigDecimal.valueOf(requestItem.quantity())));
            orderItemMapper.insert(item);
        }
    }

    private BigDecimal totalAmount(List<CreateOrderItemRequest> items,
                                   Map<Long, MenuItemSnapshotResponse> snapshotsByItemId) {
        return items.stream()
                .map(item -> requireSnapshot(item.dailyMenuItemId(), snapshotsByItemId).unitPrice()
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private MenuItemSnapshotResponse requireSnapshot(Long dailyMenuItemId,
                                                     Map<Long, MenuItemSnapshotResponse> snapshotsByItemId) {
        MenuItemSnapshotResponse snapshot = snapshotsByItemId.get(dailyMenuItemId);
        if (snapshot == null || snapshot.unitPrice() == null || snapshot.dishId() == null
                || snapshot.dishName() == null || snapshot.dishName().isBlank()) {
            throw new BusinessException(ErrorCode.DISH_NOT_AVAILABLE);
        }
        return snapshot;
    }

    private void reserveInventory(Long orderId, List<InventoryItemRequest> inventoryItems) {
        Result<InventoryOperationResponse> result = menuFeignClient.reserve(new InventoryOperationRequest(orderId,
                inventoryItems));
        if (result == null || result.getCode() == null || result.getCode() != 200 || result.getData() == null
                || !result.getData().success()) {
            throw remoteFailure(result, "库存预占失败");
        }
    }

    private Order requireExistingOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "幂等订单不存在");
        }
        return order;
    }

    private int payTimeoutMinutes() {
        OrderConfig config = orderConfigMapper.selectOne(new LambdaQueryWrapper<OrderConfig>()
                .eq(OrderConfig::getConfigKey, "pay_timeout_minutes"));
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return DEFAULT_PAY_TIMEOUT_MINUTES;
        }
        try {
            return Math.max(Integer.parseInt(config.getConfigValue()), 1);
        } catch (NumberFormatException ex) {
            return DEFAULT_PAY_TIMEOUT_MINUTES;
        }
    }

    private UserContext requireUser() {
        UserContext context = UserContextHolder.require();
        if (!context.isUser()) {
            throw new ForbiddenException();
        }
        return context;
    }

    private String requestHash(CreateOrderRequest request) {
        try {
            byte[] requestBytes = objectMapper.writeValueAsBytes(request);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(requestBytes));
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "订单幂等摘要生成失败");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }

    private BusinessException remoteFailure(Result<?> result, String defaultMessage) {
        return RemoteResultErrors.toBusinessException(result, defaultMessage);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
