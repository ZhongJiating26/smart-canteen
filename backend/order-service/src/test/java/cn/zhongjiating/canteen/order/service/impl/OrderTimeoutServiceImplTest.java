package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderConfig;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mq.OrderTimeoutMessage;
import cn.zhongjiating.canteen.order.mq.RocketMqOrderTimeoutMessagePublisher;
import cn.zhongjiating.canteen.order.service.OrderCancelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTimeoutServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderConfigMapper orderConfigMapper;
    @Mock
    private OrderCancelService orderCancelService;

    @Test
    void payTimeoutCancelsStillPendingExpiredOrder() {
        Order order = order(OrderStatus.PENDING_PAY);
        order.setCreateTime(LocalDateTime.now().minusMinutes(11));
        when(orderMapper.selectById(8000001L)).thenReturn(order);
        when(orderConfigMapper.selectOne(any())).thenReturn(config("pay_timeout_minutes", "10"));

        service().handle(message(RocketMqOrderTimeoutMessagePublisher.PAY_TIMEOUT));

        verify(orderCancelService).cancelExpiredPending(order);
    }

    @Test
    void payTimeoutIgnoresOrderAlreadyPaid() {
        Order order = order(OrderStatus.PAID);
        order.setCreateTime(LocalDateTime.now().minusMinutes(11));
        when(orderMapper.selectById(8000001L)).thenReturn(order);

        service().handle(message(RocketMqOrderTimeoutMessagePublisher.PAY_TIMEOUT));

        verify(orderCancelService, never()).cancelExpiredPending(any());
    }

    @Test
    void acceptTimeoutCancelsStillPaidExpiredOrder() {
        Order order = order(OrderStatus.PAID);
        order.setPayTime(LocalDateTime.now().minusMinutes(11));
        when(orderMapper.selectById(8000001L)).thenReturn(order);
        when(orderConfigMapper.selectOne(any())).thenReturn(config("accept_timeout_minutes", "10"));

        service().handle(message(RocketMqOrderTimeoutMessagePublisher.ACCEPT_TIMEOUT));

        verify(orderCancelService).cancelExpiredPaid(order);
    }

    @Test
    void acceptTimeoutIgnoresOrderAlreadyPreparing() {
        Order order = order(OrderStatus.PREPARING);
        order.setPayTime(LocalDateTime.now().minusMinutes(11));
        when(orderMapper.selectById(8000001L)).thenReturn(order);

        service().handle(message(RocketMqOrderTimeoutMessagePublisher.ACCEPT_TIMEOUT));

        verify(orderCancelService, never()).cancelExpiredPaid(any());
    }

    @Test
    void scanExpiredOrdersCancelsPendingAndPaidTimeoutOrders() {
        Order pending = order(OrderStatus.PENDING_PAY);
        pending.setCreateTime(LocalDateTime.now().minusMinutes(20));
        Order paid = order(OrderStatus.PAID);
        paid.setPayTime(LocalDateTime.now().minusMinutes(20));
        when(orderConfigMapper.selectOne(any()))
                .thenReturn(config("pay_timeout_minutes", "10"))
                .thenReturn(config("accept_timeout_minutes", "10"));
        when(orderMapper.selectList(any()))
                .thenReturn(List.of(pending))
                .thenReturn(List.of(paid));

        service().scanExpiredOrders();

        verify(orderCancelService).cancelExpiredPending(pending);
        verify(orderCancelService).cancelExpiredPaid(paid);
        verify(orderMapper, times(2)).selectList(any());
    }

    private OrderTimeoutServiceImpl service() {
        return new OrderTimeoutServiceImpl(orderMapper, orderConfigMapper, orderCancelService);
    }

    private OrderTimeoutMessage message(String timeoutType) {
        return new OrderTimeoutMessage(8000001L, "8000001", timeoutType, LocalDateTime.now());
    }

    private Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(8000001L);
        order.setOrderNo("8000001");
        order.setStatus(status.getCode());
        return order;
    }

    private OrderConfig config(String key, String value) {
        OrderConfig config = new OrderConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        return config;
    }
}
