package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.order.dto.PayOrderRequest;
import cn.zhongjiating.canteen.order.dto.PayPasswordVerifyResponse;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderStatusLog;
import cn.zhongjiating.canteen.order.feign.UserFeignClient;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mapper.OrderStatusLogMapper;
import cn.zhongjiating.canteen.order.mq.OrderTimeoutMessagePublisher;
import cn.zhongjiating.canteen.order.service.OrderCancelService;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPaymentServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderStatusLogMapper orderStatusLogMapper;
    @Mock
    private OrderConfigMapper orderConfigMapper;
    @Mock
    private UserFeignClient userFeignClient;
    @Mock
    private OrderCancelService orderCancelService;
    @Mock
    private OrderTimeoutMessagePublisher timeoutMessagePublisher;

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void payPendingOrderUpdatesStatusAndWritesLog() {
        UserContextHolder.set(UserContext.of(1000001L, "user", RoleEnum.USER));
        Order order = pendingOrder(LocalDateTime.now());
        when(orderMapper.selectById(8000001L)).thenReturn(order);
        when(userFeignClient.verifyPayPassword(any())).thenReturn(Result.success(new PayPasswordVerifyResponse(true)));
        when(orderMapper.markPaid(any(), any())).thenReturn(1);

        OrderStatusVO result = service().pay(8000001L, new PayOrderRequest("123456"));

        ArgumentCaptor<OrderStatusLog> logCaptor = ArgumentCaptor.forClass(OrderStatusLog.class);
        verify(orderStatusLogMapper).insert(logCaptor.capture());
        verify(timeoutMessagePublisher).publishAcceptTimeout(order);
        assertThat(result.status()).isEqualTo(OrderStatus.PAID.getCode());
        assertThat(order.getPayTime()).isNotNull();
        assertThat(logCaptor.getValue().getFromStatus()).isEqualTo(OrderStatus.PENDING_PAY.getCode());
        assertThat(logCaptor.getValue().getToStatus()).isEqualTo(OrderStatus.PAID.getCode());
        assertThat(logCaptor.getValue().getOperatorId()).isEqualTo(1000001L);
    }

    @Test
    void payExpiredPendingOrderCancelsBeforePasswordVerification() {
        UserContextHolder.set(UserContext.of(1000001L, "user", RoleEnum.USER));
        Order order = pendingOrder(LocalDateTime.now().minusMinutes(11));
        when(orderMapper.selectById(8000001L)).thenReturn(order);

        assertThatThrownBy(() -> service().pay(8000001L, new PayOrderRequest("123456")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_EXPIRED);
        verify(orderCancelService).cancelExpiredPending(order);
        verify(userFeignClient, never()).verifyPayPassword(any());
    }

    private OrderPaymentServiceImpl service() {
        return new OrderPaymentServiceImpl(orderMapper, orderStatusLogMapper, orderConfigMapper, userFeignClient,
                orderCancelService, timeoutMessagePublisher);
    }

    private Order pendingOrder(LocalDateTime createTime) {
        Order order = new Order();
        order.setId(8000001L);
        order.setOrderNo("8000001");
        order.setUserId(1000001L);
        order.setMerchantId(2000001L);
        order.setStatus(OrderStatus.PENDING_PAY.getCode());
        order.setCreateTime(createTime);
        return order;
    }
}
