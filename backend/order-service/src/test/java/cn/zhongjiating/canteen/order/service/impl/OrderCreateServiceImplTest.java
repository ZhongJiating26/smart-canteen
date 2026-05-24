package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.order.dto.CreateOrderItemRequest;
import cn.zhongjiating.canteen.order.dto.CreateOrderRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationResponse;
import cn.zhongjiating.canteen.order.dto.MenuItemSnapshotResponse;
import cn.zhongjiating.canteen.order.dto.MenuSnapshotResponse;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderItem;
import cn.zhongjiating.canteen.order.feign.MenuFeignClient;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import cn.zhongjiating.canteen.order.mapper.OrderItemMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mq.OrderTimeoutMessagePublisher;
import cn.zhongjiating.canteen.order.service.OrderIdempotencyService;
import cn.zhongjiating.canteen.order.vo.CreateOrderVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCreateServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderConfigMapper orderConfigMapper;
    @Mock
    private MenuFeignClient menuFeignClient;
    @Mock
    private OrderIdempotencyService orderIdempotencyService;
    @Mock
    private OrderTimeoutMessagePublisher timeoutMessagePublisher;

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void createStoresOrderItemsAndReservesInventory() {
        UserContextHolder.set(UserContext.of(1000001L, "user", RoleEnum.USER));
        when(orderIdempotencyService.findExistingOrderId(eq(1000001L), eq("key-1"), any())).thenReturn(null);
        when(menuFeignClient.snapshot(any())).thenReturn(Result.success(snapshot()));
        when(menuFeignClient.reserve(any())).thenReturn(Result.success(new InventoryOperationResponse(true)));

        CreateOrderVO result = service().create("key-1", request());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderMapper).insert(orderCaptor.capture());
        verify(orderItemMapper).insert(itemCaptor.capture());
        verify(menuFeignClient).reserve(any());
        verify(timeoutMessagePublisher).publishPayTimeout(orderCaptor.getValue());
        verify(orderIdempotencyService).saveAfterCommit(eq(1000001L), eq("key-1"), any(), eq(result.orderId()));
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo("PENDING_PAY");
        assertThat(orderCaptor.getValue().getRemark()).isEqualTo("少辣");
        assertThat(orderCaptor.getValue().getTotalAmount()).isEqualByComparingTo("24.00");
        assertThat(itemCaptor.getValue().getDishName()).isEqualTo("番茄鸡蛋盖饭");
        assertThat(itemCaptor.getValue().getSubtotal()).isEqualByComparingTo("24.00");
        assertThat(result.expireTime()).isEqualTo(orderCaptor.getValue().getCreateTime().plusMinutes(10));
    }

    @Test
    void duplicateRequestReturnsExistingOrderWithoutRemoteCalls() {
        UserContextHolder.set(UserContext.of(1000001L, "user", RoleEnum.USER));
        Order existing = existingOrder();
        when(orderIdempotencyService.findExistingOrderId(eq(1000001L), eq("key-1"), any())).thenReturn(8000001L);
        when(orderMapper.selectById(8000001L)).thenReturn(existing);

        CreateOrderVO result = service().create("key-1", request());

        assertThat(result.orderId()).isEqualTo(8000001L);
        verify(menuFeignClient, never()).snapshot(any());
        verify(menuFeignClient, never()).reserve(any());
        verify(timeoutMessagePublisher, never()).publishPayTimeout(any());
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
    }

    @Test
    void createRejectsMerchantRole() {
        UserContextHolder.set(UserContext.of(1000002L, "merchant", RoleEnum.MERCHANT));

        assertThatThrownBy(() -> service().create("key-1", request()))
                .isInstanceOf(ForbiddenException.class);
        verify(orderMapper, never()).insert(any(Order.class));
    }

    private OrderCreateServiceImpl service() {
        return new OrderCreateServiceImpl(orderMapper, orderItemMapper, orderConfigMapper, menuFeignClient,
                orderIdempotencyService, timeoutMessagePublisher, new ObjectMapper());
    }

    private CreateOrderRequest request() {
        return new CreateOrderRequest(2000001L, " 少辣 ",
                List.of(new CreateOrderItemRequest(7000001L, 2)));
    }

    private MenuSnapshotResponse snapshot() {
        return new MenuSnapshotResponse(2000001L, List.of(new MenuItemSnapshotResponse(7000001L, 5000001L,
                "番茄鸡蛋盖饭", null, new BigDecimal("12.00"))));
    }

    private Order existingOrder() {
        Order order = new Order();
        order.setId(8000001L);
        order.setOrderNo("8000001");
        order.setStatus("PENDING_PAY");
        order.setTotalAmount(new BigDecimal("12.00"));
        order.setCreateTime(LocalDateTime.of(2026, 5, 22, 12, 0));
        return order;
    }
}
