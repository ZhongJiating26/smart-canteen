package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.order.dto.CancelOrderRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationResponse;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderItem;
import cn.zhongjiating.canteen.order.entity.OrderStatusLog;
import cn.zhongjiating.canteen.order.feign.MenuFeignClient;
import cn.zhongjiating.canteen.order.feign.PickupFeignClient;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import cn.zhongjiating.canteen.order.mapper.OrderItemMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mapper.OrderStatusLogMapper;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import org.junit.jupiter.api.AfterEach;
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
class OrderCancelServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderStatusLogMapper orderStatusLogMapper;
    @Mock
    private OrderConfigMapper orderConfigMapper;
    @Mock
    private MenuFeignClient menuFeignClient;
    @Mock
    private PickupFeignClient pickupFeignClient;

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void cancelPaidOrderReleasesInventoryWithoutRemovingQueue() {
        UserContextHolder.set(UserContext.of(1000001L, "user", RoleEnum.USER));
        Order order = paidOrder();
        when(orderMapper.selectById(8000001L)).thenReturn(order);
        when(orderMapper.cancelIfStatus(any(), any(), any(), any())).thenReturn(1);
        when(orderItemMapper.selectByOrderId(8000001L)).thenReturn(List.of(orderItem()));
        when(menuFeignClient.release(any())).thenReturn(Result.success(new InventoryOperationResponse(true)));

        OrderStatusVO result = service().cancelByUser(8000001L, new CancelOrderRequest(" 不想吃了 "));

        ArgumentCaptor<OrderStatusLog> logCaptor = ArgumentCaptor.forClass(OrderStatusLog.class);
        verify(menuFeignClient).release(any());
        verify(pickupFeignClient, never()).removeQueue(any());
        verify(orderStatusLogMapper).insert(logCaptor.capture());
        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED.getCode());
        assertThat(order.getCancelReason()).isEqualTo("不想吃了");
        assertThat(logCaptor.getValue().getFromStatus()).isEqualTo(OrderStatus.PAID.getCode());
        assertThat(logCaptor.getValue().getToStatus()).isEqualTo(OrderStatus.CANCELLED.getCode());
    }

    @Test
    void cancelPreparingOrderRemovesQueue() {
        UserContextHolder.set(UserContext.of(1000001L, "user", RoleEnum.USER));
        Order order = paidOrder();
        order.setStatus(OrderStatus.PREPARING.getCode());
        order.setAcceptTime(java.time.LocalDateTime.now());
        when(orderMapper.selectById(8000001L)).thenReturn(order);
        when(orderMapper.cancelIfStatus(any(), any(), any(), any())).thenReturn(1);
        when(orderItemMapper.selectByOrderId(8000001L)).thenReturn(List.of(orderItem()));
        when(menuFeignClient.release(any())).thenReturn(Result.success(new InventoryOperationResponse(true)));
        when(pickupFeignClient.removeQueue(any())).thenReturn(Result.success());

        service().cancelByUser(8000001L, new CancelOrderRequest("临时取消"));

        verify(pickupFeignClient).removeQueue(any());
    }

    private OrderCancelServiceImpl service() {
        return new OrderCancelServiceImpl(orderMapper, orderItemMapper, orderStatusLogMapper, orderConfigMapper,
                menuFeignClient, pickupFeignClient);
    }

    private Order paidOrder() {
        Order order = new Order();
        order.setId(8000001L);
        order.setOrderNo("8000001");
        order.setUserId(1000001L);
        order.setMerchantId(2000001L);
        order.setStatus(OrderStatus.PAID.getCode());
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
