package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.common.util.SnowflakeIdUtil;
import cn.zhongjiating.canteen.order.dto.InventoryItemRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationResponse;
import cn.zhongjiating.canteen.order.dto.MerchantLookupResponse;
import cn.zhongjiating.canteen.order.dto.PickupNoResponse;
import cn.zhongjiating.canteen.order.dto.PickupPreparingRequest;
import cn.zhongjiating.canteen.order.dto.PickupQrcodeResponse;
import cn.zhongjiating.canteen.order.dto.PickupQueueRequest;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderConfig;
import cn.zhongjiating.canteen.order.entity.OrderItem;
import cn.zhongjiating.canteen.order.entity.OrderStatusLog;
import cn.zhongjiating.canteen.order.feign.MenuFeignClient;
import cn.zhongjiating.canteen.order.feign.PickupFeignClient;
import cn.zhongjiating.canteen.order.feign.RemoteResultErrors;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import cn.zhongjiating.canteen.order.mapper.OrderItemMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mapper.OrderStatusLogMapper;
import cn.zhongjiating.canteen.order.service.OrderMerchantService;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderMerchantServiceImpl implements OrderMerchantService {

    private static final int DEFAULT_ACCEPT_TIMEOUT_MINUTES = 10;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final OrderConfigMapper orderConfigMapper;
    private final MenuFeignClient menuFeignClient;
    private final PickupFeignClient pickupFeignClient;

    public OrderMerchantServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                                    OrderStatusLogMapper orderStatusLogMapper, OrderConfigMapper orderConfigMapper,
                                    MenuFeignClient menuFeignClient, PickupFeignClient pickupFeignClient) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
        this.orderConfigMapper = orderConfigMapper;
        this.menuFeignClient = menuFeignClient;
        this.pickupFeignClient = pickupFeignClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderStatusVO accept(Long orderId) {
        MerchantContext merchant = requireMerchantOrder(orderId, OrderStatus.PAID);
        Order order = merchant.order();
        if (isAcceptExpired(order)) {
            cancel(order, merchant.user(), "商家接单超时自动取消");
            throw new BusinessException(ErrorCode.ORDER_EXPIRED, "订单接单超时，已自动取消");
        }

        Result<PickupNoResponse> pickupResult = pickupFeignClient.enqueuePreparing(
                new PickupPreparingRequest(order.getMerchantId(), order.getId()));
        PickupNoResponse pickup = requireRemoteData(pickupResult, "加入制作中队列失败");

        LocalDateTime acceptTime = LocalDateTime.now();
        int rows = orderMapper.markPreparing(order.getId(), pickup.pickupNo(), acceptTime);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS);
        }
        order.setStatus(OrderStatus.PREPARING.getCode());
        order.setPickupNo(pickup.pickupNo());
        order.setAcceptTime(acceptTime);
        writeStatusLog(order, OrderStatus.PAID, OrderStatus.PREPARING, merchant.user(), "商家接单");
        return OrderStatusVO.from(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderStatusVO reject(Long orderId) {
        MerchantContext merchant = requireMerchantOrder(orderId, OrderStatus.PAID);
        Order order = merchant.order();
        cancel(order, merchant.user(), "商家拒单");
        return OrderStatusVO.from(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderStatusVO ready(Long orderId) {
        MerchantContext merchant = requireMerchantOrder(orderId, OrderStatus.PREPARING);
        Order order = merchant.order();
        Result<PickupQrcodeResponse> qrcodeResult = pickupFeignClient.enqueueWaiting(
                new PickupQueueRequest(order.getMerchantId(), order.getId()));
        PickupQrcodeResponse qrcode = requireRemoteData(qrcodeResult, "转入待取餐队列失败");

        int rows = orderMapper.markReady(order.getId(), qrcode.qrcodeId());
        if (rows != 1) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS);
        }
        order.setStatus(OrderStatus.READY.getCode());
        order.setPickupNo(qrcode.pickupNo());
        order.setPickupQrcodeId(qrcode.qrcodeId());
        writeStatusLog(order, OrderStatus.PREPARING, OrderStatus.READY, merchant.user(), "商家出餐");
        return OrderStatusVO.from(order);
    }

    private MerchantContext requireMerchantOrder(Long orderId, OrderStatus expectedStatus) {
        UserContext user = requireMerchantUser();
        MerchantLookupResponse merchant = requireCurrentMerchant(user.getUserId());
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
        }
        if (!merchant.merchantId().equals(order.getMerchantId())) {
            throw new ForbiddenException();
        }
        if (!expectedStatus.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS);
        }
        return new MerchantContext(user, merchant, order);
    }

    private UserContext requireMerchantUser() {
        UserContext context = UserContextHolder.require();
        if (!context.isMerchant()) {
            throw new ForbiddenException();
        }
        return context;
    }

    private MerchantLookupResponse requireCurrentMerchant(Long userId) {
        Result<MerchantLookupResponse> result = menuFeignClient.merchantByUserId(userId);
        MerchantLookupResponse merchant = requireRemoteData(result, "商家信息查询失败");
        if (merchant.status() == null || merchant.status() != 1) {
            throw new ForbiddenException("商家窗口不可用");
        }
        return merchant;
    }

    private boolean isAcceptExpired(Order order) {
        return order.getPayTime() != null
                && LocalDateTime.now().isAfter(order.getPayTime().plusMinutes(acceptTimeoutMinutes()));
    }

    private int acceptTimeoutMinutes() {
        OrderConfig config = orderConfigMapper.selectOne(new LambdaQueryWrapper<OrderConfig>()
                .eq(OrderConfig::getConfigKey, "accept_timeout_minutes"));
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return DEFAULT_ACCEPT_TIMEOUT_MINUTES;
        }
        try {
            return Math.max(Integer.parseInt(config.getConfigValue()), 1);
        } catch (NumberFormatException ex) {
            return DEFAULT_ACCEPT_TIMEOUT_MINUTES;
        }
    }

    private void cancel(Order order, UserContext operator, String reason) {
        String fromStatus = order.getStatus();
        LocalDateTime cancelTime = LocalDateTime.now();
        int rows = orderMapper.cancelIfStatus(order.getId(), OrderStatus.PAID.getCode(), reason, cancelTime);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS);
        }
        releaseInventory(order.getId());
        removeQueue(order);
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelReason(reason);
        order.setCancelTime(cancelTime);
        writeStatusLog(order, OrderStatus.valueOf(fromStatus), OrderStatus.CANCELLED, operator, reason);
    }

    private void releaseInventory(Long orderId) {
        List<InventoryItemRequest> items = orderItemMapper.selectByOrderId(orderId)
                .stream()
                .map(item -> new InventoryItemRequest(item.getDailyMenuItemId(), item.getQuantity()))
                .toList();
        if (items.isEmpty() || items.stream().anyMatch(item -> item.dailyMenuItemId() == null)) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "订单明细缺少库存释放项");
        }
        Result<InventoryOperationResponse> result = menuFeignClient.release(new InventoryOperationRequest(orderId, items));
        InventoryOperationResponse response = requireRemoteData(result, "库存释放失败");
        if (!response.success()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "库存释放失败");
        }
    }

    private void removeQueue(Order order) {
        Result<Void> result = pickupFeignClient.removeQueue(new PickupQueueRequest(order.getMerchantId(), order.getId()));
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            throw remoteFailure(result, "取餐队列移除失败");
        }
    }

    private void writeStatusLog(Order order, OrderStatus fromStatus, OrderStatus toStatus, UserContext operator,
                                String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setId(SnowflakeIdUtil.nextId());
        log.setOrderId(order.getId());
        log.setFromStatus(fromStatus.getCode());
        log.setToStatus(toStatus.getCode());
        log.setOperatorId(operator.getUserId());
        log.setOperatorRole(operator.getRole().getCode());
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        orderStatusLogMapper.insert(log);
    }

    private <T> T requireRemoteData(Result<T> result, String defaultMessage) {
        if (result == null || result.getCode() == null || result.getCode() != 200 || result.getData() == null) {
            throw remoteFailure(result, defaultMessage);
        }
        return result.getData();
    }

    private BusinessException remoteFailure(Result<?> result, String defaultMessage) {
        return RemoteResultErrors.toBusinessException(result, defaultMessage);
    }

    private record MerchantContext(UserContext user, MerchantLookupResponse merchant, Order order) {
    }
}
