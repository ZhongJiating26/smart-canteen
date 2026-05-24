package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.order.dto.InventoryOperationResponse;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderItem;
import cn.zhongjiating.canteen.order.entity.OrderStatusLog;
import cn.zhongjiating.canteen.order.feign.MenuFeignClient;
import cn.zhongjiating.canteen.order.mapper.OrderItemMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mapper.OrderStatusLogMapper;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCompleteServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderStatusLogMapper orderStatusLogMapper;
    @Mock
    private MenuFeignClient menuFeignClient;

    @Test
    void completeReadyOrderConsumesInventoryAndMarksCompleted() {
        Order order = order(OrderStatus.READY);
        when(orderMapper.selectById(8000001L)).thenReturn(order);
        when(orderItemMapper.selectByOrderId(8000001L)).thenReturn(List.of(orderItem()));
        when(menuFeignClient.consume(any())).thenReturn(Result.success(new InventoryOperationResponse(true)));
        when(orderMapper.markCompleted(any(), any())).thenReturn(1);

        OrderStatusVO result = service().complete(8000001L);

        ArgumentCaptor<OrderStatusLog> logCaptor = ArgumentCaptor.forClass(OrderStatusLog.class);
        verify(menuFeignClient).consume(any());
        verify(orderMapper).markCompleted(any(), any());
        verify(orderStatusLogMapper).insert(logCaptor.capture());
        assertThat(result.status()).isEqualTo(OrderStatus.COMPLETED.getCode());
        assertThat(logCaptor.getValue().getFromStatus()).isEqualTo(OrderStatus.READY.getCode());
        assertThat(logCaptor.getValue().getToStatus()).isEqualTo(OrderStatus.COMPLETED.getCode());
        assertThat(logCaptor.getValue().getOperatorRole()).isEqualTo("SYSTEM");
    }

    @Test
    void completeAlreadyCompletedOrderIsIdempotent() {
        Order order = order(OrderStatus.COMPLETED);
        when(orderMapper.selectById(8000001L)).thenReturn(order);

        OrderStatusVO result = service().complete(8000001L);

        verify(menuFeignClient, never()).consume(any());
        verify(orderMapper, never()).markCompleted(any(), any());
        assertThat(result.status()).isEqualTo(OrderStatus.COMPLETED.getCode());
    }

    private OrderCompleteServiceImpl service() {
        return new OrderCompleteServiceImpl(orderMapper, orderItemMapper, orderStatusLogMapper, menuFeignClient);
    }

    private Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(8000001L);
        order.setOrderNo("8000001");
        order.setMerchantId(2000001L);
        order.setStatus(status.getCode());
        order.setPickupNo("A001");
        order.setPickupQrcodeId(9000001L);
        return order;
    }

    private OrderItem orderItem() {
        OrderItem item = new OrderItem();
        item.setOrderId(8000001L);
        item.setDailyMenuItemId(7000001L);
        item.setQuantity(2);
        return item;
    }
}
