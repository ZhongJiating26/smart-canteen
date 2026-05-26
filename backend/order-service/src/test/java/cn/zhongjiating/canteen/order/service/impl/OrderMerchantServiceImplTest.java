package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.order.dto.InventoryOperationResponse;
import cn.zhongjiating.canteen.order.dto.MerchantLookupResponse;
import cn.zhongjiating.canteen.order.dto.PickupNoResponse;
import cn.zhongjiating.canteen.order.dto.PickupQrcodeResponse;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMerchantServiceImplTest {

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
    void acceptPaidOrderEnqueuesPreparingAndStoresPickupNo() {
        UserContextHolder.set(UserContext.of(1000002L, "merchant", RoleEnum.MERCHANT));
        Order order = order(OrderStatus.PAID);
        order.setPickupNo("A001");
        when(menuFeignClient.merchantByUserId(1000002L)).thenReturn(Result.success(merchant()));
        when(orderMapper.selectById(8000001L)).thenReturn(order);
        when(orderMapper.markPreparing(any(), any())).thenReturn(1);

        OrderStatusVO result = service().accept(8000001L);

        ArgumentCaptor<OrderStatusLog> logCaptor = ArgumentCaptor.forClass(OrderStatusLog.class);
        verify(orderMapper).markPreparing(8000001L, order.getAcceptTime());
        verify(orderStatusLogMapper).insert(logCaptor.capture());
        assertThat(result.status()).isEqualTo(OrderStatus.PREPARING.getCode());
        assertThat(result.pickupNo()).isEqualTo("A001");
        assertThat(logCaptor.getValue().getFromStatus()).isEqualTo(OrderStatus.PAID.getCode());
        assertThat(logCaptor.getValue().getToStatus()).isEqualTo(OrderStatus.PREPARING.getCode());
    }

    @Test
    void rejectPaidOrderCancelsAndReleasesInventory() {
        UserContextHolder.set(UserContext.of(1000002L, "merchant", RoleEnum.MERCHANT));
        Order order = order(OrderStatus.PAID);
        when(menuFeignClient.merchantByUserId(1000002L)).thenReturn(Result.success(merchant()));
        when(orderMapper.selectById(8000001L)).thenReturn(order);
        when(orderMapper.cancelIfStatus(any(), any(), any(), any())).thenReturn(1);
        when(orderItemMapper.selectByOrderId(8000001L)).thenReturn(List.of(orderItem()));
        when(menuFeignClient.release(any())).thenReturn(Result.success(new InventoryOperationResponse(true)));

        OrderStatusVO result = service().reject(8000001L);

        verify(menuFeignClient).release(any());
        verify(pickupFeignClient, never()).removeQueue(any());
        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED.getCode());
        assertThat(order.getCancelReason()).isEqualTo("商家拒单");
    }

    @Test
    void readyPreparingOrderMovesToWaitingAndStoresQrcodeId() {
        UserContextHolder.set(UserContext.of(1000002L, "merchant", RoleEnum.MERCHANT));
        Order order = order(OrderStatus.PREPARING);
        order.setPickupNo("A001");
        when(menuFeignClient.merchantByUserId(1000002L)).thenReturn(Result.success(merchant()));
        when(orderMapper.selectById(8000001L)).thenReturn(order);
        when(pickupFeignClient.enqueueWaiting(any())).thenReturn(Result.success(
                new PickupQrcodeResponse(9000001L, 8000001L, "A001", "code", "{}", false, LocalDateTime.now())));
        when(orderMapper.markReady(8000001L, 9000001L)).thenReturn(1);

        OrderStatusVO result = service().ready(8000001L);

        verify(orderMapper).markReady(8000001L, 9000001L);
        assertThat(result.status()).isEqualTo(OrderStatus.READY.getCode());
        assertThat(result.hasQrcode()).isTrue();
        assertThat(order.getPickupQrcodeId()).isEqualTo(9000001L);
    }

    private OrderMerchantServiceImpl service() {
        return new OrderMerchantServiceImpl(orderMapper, orderItemMapper, orderStatusLogMapper, orderConfigMapper,
                menuFeignClient, pickupFeignClient);
    }

    private MerchantLookupResponse merchant() {
        return new MerchantLookupResponse(2000001L, 1000002L, "一号窗口", 1);
    }

    private Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(8000001L);
        order.setOrderNo("8000001");
        order.setUserId(1000001L);
        order.setMerchantId(2000001L);
        order.setStatus(status.getCode());
        order.setPayTime(LocalDateTime.now());
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
