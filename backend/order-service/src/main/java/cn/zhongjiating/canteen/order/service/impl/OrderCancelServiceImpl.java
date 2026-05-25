package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.common.util.SnowflakeIdUtil;
import cn.zhongjiating.canteen.order.dto.CancelOrderRequest;
import cn.zhongjiating.canteen.order.dto.InventoryItemRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationResponse;
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
import cn.zhongjiating.canteen.order.service.OrderCancelService;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderCancelServiceImpl implements OrderCancelService {

    private static final int DEFAULT_CANCEL_WINDOW_MINUTES = 1;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final OrderConfigMapper orderConfigMapper;
    private final MenuFeignClient menuFeignClient;
    private final PickupFeignClient pickupFeignClient;

    public OrderCancelServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
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
    public OrderStatusVO cancelByUser(Long orderId, CancelOrderRequest request) {
        UserContext user = requireUser();
        Order order = requireUserOrder(orderId, user.getUserId());
        ensureUserCanCancel(order);
        cancel(order, user.getUserId(), user.getRole().getCode(), trimToNull(request == null ? null : request.reason()));
        return OrderStatusVO.from(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void cancelExpiredPending(Order order) {
        if (!OrderStatus.PENDING_PAY.getCode().equals(order.getStatus())) {
            return;
        }
        cancel(order, 0L, "SYSTEM", "支付超时自动取消");
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void cancelExpiredPaid(Order order) {
        if (!OrderStatus.PAID.getCode().equals(order.getStatus())) {
            return;
        }
        cancel(order, 0L, "SYSTEM", "接单超时自动取消");
    }

    private void cancel(Order order, Long operatorId, String operatorRole, String reason) {
        String fromStatus = order.getStatus();
        LocalDateTime cancelTime = LocalDateTime.now();
        int rows = orderMapper.cancelIfStatus(order.getId(), fromStatus, reason, cancelTime);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS);
        }
        releaseInventory(order.getId());
        removeQueue(order);
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelReason(reason);
        order.setCancelTime(cancelTime);
        writeStatusLog(order, fromStatus, operatorId, operatorRole, reason);
    }

    private void releaseInventory(Long orderId) {
        List<InventoryItemRequest> items = orderItemMapper.selectByOrderId(orderId)
                .stream()
                .map(item -> new InventoryItemRequest(item.getDailyMenuItemId(), item.getQuantity()))
                .toList();
        if (items.isEmpty() || items.stream().anyMatch(item -> item.dailyMenuItemId() == null)) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "订单明细缺少库存释放项");
        }
        Result<InventoryOperationResponse> result = menuFeignClient.release(new InventoryOperationRequest(orderId,
                items));
        if (result == null || result.getCode() == null || result.getCode() != 200 || result.getData() == null
                || !result.getData().success()) {
            throw remoteFailure(result, "库存释放失败");
        }
    }

    private void removeQueue(Order order) {
        Result<Void> result = pickupFeignClient.removeQueue(new PickupQueueRequest(order.getMerchantId(), order.getId()));
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            throw remoteFailure(result, "取餐队列移除失败");
        }
    }

    private void ensureUserCanCancel(Order order) {
        if (OrderStatus.PENDING_PAY.getCode().equals(order.getStatus()) || OrderStatus.PAID.getCode().equals(order.getStatus())) {
            return;
        }
        if (OrderStatus.PREPARING.getCode().equals(order.getStatus()) && order.getAcceptTime() != null
                && !LocalDateTime.now().isAfter(order.getAcceptTime().plusMinutes(cancelWindowMinutes()))) {
            return;
        }
        throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
    }

    private int cancelWindowMinutes() {
        OrderConfig config = orderConfigMapper.selectOne(new LambdaQueryWrapper<OrderConfig>()
                .eq(OrderConfig::getConfigKey, "cancel_window_minutes"));
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return DEFAULT_CANCEL_WINDOW_MINUTES;
        }
        try {
            return Math.max(Integer.parseInt(config.getConfigValue()), 1);
        } catch (NumberFormatException ex) {
            return DEFAULT_CANCEL_WINDOW_MINUTES;
        }
    }

    private void writeStatusLog(Order order, String fromStatus, Long operatorId, String operatorRole, String reason) {
        OrderStatusLog log = new OrderStatusLog();
        log.setId(SnowflakeIdUtil.nextId());
        log.setOrderId(order.getId());
        log.setFromStatus(fromStatus);
        log.setToStatus(OrderStatus.CANCELLED.getCode());
        log.setOperatorId(operatorId);
        log.setOperatorRole(operatorRole);
        log.setRemark(reason);
        log.setCreateTime(order.getCancelTime());
        orderStatusLogMapper.insert(log);
    }

    private Order requireUserOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
        }
        if (!userId.equals(order.getUserId())) {
            throw new ForbiddenException();
        }
        return order;
    }

    private UserContext requireUser() {
        UserContext context = UserContextHolder.require();
        if (!context.isUser()) {
            throw new ForbiddenException();
        }
        return context;
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
