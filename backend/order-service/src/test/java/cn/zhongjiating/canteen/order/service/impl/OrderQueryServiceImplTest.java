package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.order.dto.MerchantLookupResponse;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderItem;
import cn.zhongjiating.canteen.order.entity.OrderStatusLog;
import cn.zhongjiating.canteen.order.feign.MenuFeignClient;
import cn.zhongjiating.canteen.order.mapper.OrderItemMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mapper.OrderStatusLogMapper;
import cn.zhongjiating.canteen.order.vo.OrderDetailVO;
import cn.zhongjiating.canteen.order.vo.OrderSummaryVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderStatusLogMapper orderStatusLogMapper;
    @Mock
    private MenuFeignClient menuFeignClient;
    @InjectMocks
    private OrderQueryServiceImpl orderQueryService;

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void myOrdersReturnsPagedSummariesWithoutStatusFilter() {
        UserContextHolder.set(UserContext.of(1000001L, "user", RoleEnum.USER));
        Order order = order(8000001L, 1000001L, 2000001L, "PENDING_PAY");
        when(orderMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<Order> requestedPage = invocation.getArgument(0);
            requestedPage.setRecords(List.of(order));
            requestedPage.setTotal(1L);
            return requestedPage;
        });

        PageResult<OrderSummaryVO> result = orderQueryService.myOrders(0L, 500L, null);

        assertThat(result.getCurrent()).isEqualTo(1L);
        assertThat(result.getSize()).isEqualTo(100L);
        assertThat(result.getRecords()).extracting(OrderSummaryVO::orderId).containsExactly(8000001L);
    }

    @Test
    void statusRejectsForeignUserOrder() {
        UserContextHolder.set(UserContext.of(1000001L, "user", RoleEnum.USER));
        when(orderMapper.selectById(8000002L)).thenReturn(order(8000002L, 1000002L, 2000001L, "PAID"));

        assertThatThrownBy(() -> orderQueryService.status(8000002L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void detailAssemblesItemsAndTimelineForOwner() {
        UserContextHolder.set(UserContext.of(1000001L, "user", RoleEnum.USER));
        when(orderMapper.selectById(8000001L)).thenReturn(order(8000001L, 1000001L, 2000001L, "PREPARING"));
        when(orderItemMapper.selectList(any())).thenReturn(List.of(item()));
        when(orderStatusLogMapper.selectList(any())).thenReturn(List.of(statusLog()));

        OrderDetailVO detail = orderQueryService.detail(8000001L);

        assertThat(detail.items()).hasSize(1);
        assertThat(detail.items().get(0).dishName()).isEqualTo("番茄鸡蛋盖饭");
        assertThat(detail.statusTimeline()).hasSize(1);
        assertThat(detail.statusTimeline().get(0).toStatus()).isEqualTo("PREPARING");
    }

    @Test
    void merchantListRejectsForeignMerchantScope() {
        UserContextHolder.set(UserContext.of(1000002L, "merchant", RoleEnum.MERCHANT));
        when(menuFeignClient.merchantByUserId(1000002L))
                .thenReturn(Result.success(new MerchantLookupResponse(2000001L, 1000002L, "一号窗口", 1)));

        assertThatThrownBy(() -> orderQueryService.merchantOrders(null, 2000002L))
                .isInstanceOf(ForbiddenException.class);
        verify(orderMapper, never()).selectList(any());
    }

    @Test
    void invalidStatusIsRejectedBeforeQuerying() {
        UserContextHolder.set(UserContext.of(1000001L, "user", RoleEnum.USER));

        assertThatThrownBy(() -> orderQueryService.myOrders(1L, 20L, "unknown"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("订单状态不正确");
        verify(orderMapper, never()).selectPage(any(), any());
    }

    private Order order(Long id, Long userId, Long merchantId, String status) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo("202605220001");
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("12.00"));
        order.setCreateTime(LocalDateTime.of(2026, 5, 22, 12, 0));
        return order;
    }

    private OrderItem item() {
        OrderItem item = new OrderItem();
        item.setDishId(5000001L);
        item.setDishName("番茄鸡蛋盖饭");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("12.00"));
        item.setSubtotal(new BigDecimal("12.00"));
        return item;
    }

    private OrderStatusLog statusLog() {
        OrderStatusLog log = new OrderStatusLog();
        log.setFromStatus("PAID");
        log.setToStatus("PREPARING");
        log.setOperatorId(1000002L);
        log.setOperatorRole("MERCHANT");
        log.setCreateTime(LocalDateTime.of(2026, 5, 22, 12, 5));
        return log;
    }
}
