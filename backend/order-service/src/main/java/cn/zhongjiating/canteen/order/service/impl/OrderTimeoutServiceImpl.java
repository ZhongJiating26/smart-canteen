package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderConfig;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mq.OrderTimeoutMessage;
import cn.zhongjiating.canteen.order.mq.RocketMqOrderTimeoutMessagePublisher;
import cn.zhongjiating.canteen.order.service.OrderCancelService;
import cn.zhongjiating.canteen.order.service.OrderTimeoutService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderTimeoutServiceImpl implements OrderTimeoutService {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutServiceImpl.class);

    private static final int DEFAULT_PAY_TIMEOUT_MINUTES = 10;
    private static final int DEFAULT_ACCEPT_TIMEOUT_MINUTES = 10;

    private final OrderMapper orderMapper;
    private final OrderConfigMapper orderConfigMapper;
    private final OrderCancelService orderCancelService;

    public OrderTimeoutServiceImpl(OrderMapper orderMapper, OrderConfigMapper orderConfigMapper,
                                   OrderCancelService orderCancelService) {
        this.orderMapper = orderMapper;
        this.orderConfigMapper = orderConfigMapper;
        this.orderCancelService = orderCancelService;
    }

    @Override
    public void handle(OrderTimeoutMessage message) {
        if (message == null || message.orderId() == null || message.timeoutType() == null) {
            return;
        }
        Order order = orderMapper.selectById(message.orderId());
        if (order == null) {
            return;
        }
        if (RocketMqOrderTimeoutMessagePublisher.PAY_TIMEOUT.equals(message.timeoutType())) {
            handlePayTimeout(order);
        } else if (RocketMqOrderTimeoutMessagePublisher.ACCEPT_TIMEOUT.equals(message.timeoutType())) {
            handleAcceptTimeout(order);
        }
    }

    @Override
    public void scanExpiredOrders() {
        int payTimeoutMinutes = configMinutes("pay_timeout_minutes", DEFAULT_PAY_TIMEOUT_MINUTES);
        int acceptTimeoutMinutes = configMinutes("accept_timeout_minutes", DEFAULT_ACCEPT_TIMEOUT_MINUTES);
        LocalDateTime now = LocalDateTime.now();
        List<Order> pendingOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.PENDING_PAY.getCode())
                .le(Order::getCreateTime, now.minusMinutes(payTimeoutMinutes))
                .orderByAsc(Order::getCreateTime)
                .last("LIMIT 100"));
        pendingOrders.forEach(this::cancelExpiredPendingQuietly);

        List<Order> paidOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.PAID.getCode())
                .le(Order::getPayTime, now.minusMinutes(acceptTimeoutMinutes))
                .orderByAsc(Order::getPayTime)
                .last("LIMIT 100"));
        paidOrders.forEach(this::cancelExpiredPaidQuietly);
    }

    private void cancelExpiredPendingQuietly(Order order) {
        try {
            orderCancelService.cancelExpiredPending(order);
        } catch (BusinessException ex) {
            log.warn("Cancel expired pending order failed, orderId={}, message={}", order.getId(), ex.getMessage());
        }
    }

    private void cancelExpiredPaidQuietly(Order order) {
        try {
            orderCancelService.cancelExpiredPaid(order);
        } catch (BusinessException ex) {
            log.warn("Cancel expired paid order failed, orderId={}, message={}", order.getId(), ex.getMessage());
        }
    }

    private void handlePayTimeout(Order order) {
        if (!OrderStatus.PENDING_PAY.getCode().equals(order.getStatus()) || order.getCreateTime() == null) {
            return;
        }
        if (!LocalDateTime.now().isBefore(order.getCreateTime().plusMinutes(configMinutes(
                "pay_timeout_minutes", DEFAULT_PAY_TIMEOUT_MINUTES)))) {
            orderCancelService.cancelExpiredPending(order);
        }
    }

    private void handleAcceptTimeout(Order order) {
        if (!OrderStatus.PAID.getCode().equals(order.getStatus()) || order.getPayTime() == null) {
            return;
        }
        if (!LocalDateTime.now().isBefore(order.getPayTime().plusMinutes(configMinutes(
                "accept_timeout_minutes", DEFAULT_ACCEPT_TIMEOUT_MINUTES)))) {
            orderCancelService.cancelExpiredPaid(order);
        }
    }

    private int configMinutes(String configKey, int defaultValue) {
        OrderConfig config = orderConfigMapper.selectOne(new LambdaQueryWrapper<OrderConfig>()
                .eq(OrderConfig::getConfigKey, configKey));
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return defaultValue;
        }
        try {
            return Math.max(Integer.parseInt(config.getConfigValue()), 1);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
