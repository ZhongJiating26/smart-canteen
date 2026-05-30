package cn.zhongjiating.canteen.order.controller;

import cn.zhongjiating.canteen.order.config.OrderUserContextInterceptor;
import cn.zhongjiating.canteen.order.config.WebMvcConfig;
import cn.zhongjiating.canteen.order.dto.CreateOrderItemRequest;
import cn.zhongjiating.canteen.order.dto.CreateOrderRequest;
import cn.zhongjiating.canteen.order.dto.PayOrderRequest;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import cn.zhongjiating.canteen.order.mapper.OrderItemMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mapper.OrderStatusLogMapper;
import cn.zhongjiating.canteen.order.service.OrderCancelService;
import cn.zhongjiating.canteen.order.service.OrderCreateService;
import cn.zhongjiating.canteen.order.service.OrderPaymentService;
import cn.zhongjiating.canteen.order.service.OrderQueryService;
import cn.zhongjiating.canteen.order.vo.CreateOrderVO;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import({OrderUserContextInterceptor.class, WebMvcConfig.class})
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderQueryService orderQueryService;

    @MockBean
    private OrderCreateService orderCreateService;

    @MockBean
    private OrderPaymentService orderPaymentService;

    @MockBean
    private OrderCancelService orderCancelService;

    // Mock MyBatis mappers to override their bean definitions and avoid sqlSessionFactory initialization issues
    @MockBean
    private OrderMapper orderMapper;

    @MockBean
    private OrderItemMapper orderItemMapper;

    @MockBean
    private OrderConfigMapper orderConfigMapper;

    @MockBean
    private OrderStatusLogMapper orderStatusLogMapper;

    @Test
    void createOrder_ValidRequest_ReturnsSuccess() throws Exception {
        CreateOrderItemRequest itemRequest = new CreateOrderItemRequest(2001L, 2);
        CreateOrderRequest request = new CreateOrderRequest(1001L, "Less spicy", Collections.singletonList(itemRequest));
        CreateOrderVO vo = new CreateOrderVO(80001L, "ORD-2026-0001", new BigDecimal("32.50"), "UNPAID", LocalDateTime.now().plusMinutes(15));

        when(orderCreateService.create(eq("idemp-key-123"), any(CreateOrderRequest.class))).thenReturn(vo);

        mockMvc.perform(post("/api/order/create")
                        .header("X-Idempotent-Key", "idemp-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("ORD-2026-0001"))
                .andExpect(jsonPath("$.data.status").value("UNPAID"));

        verify(orderCreateService).create(eq("idemp-key-123"), any(CreateOrderRequest.class));
    }

    @Test
    void payOrder_ValidRequest_ReturnsSuccess() throws Exception {
        PayOrderRequest request = new PayOrderRequest("123456");
        OrderStatusVO statusVO = new OrderStatusVO(80001L, "ORD-2026-0001", "PAID", "A-01", true);

        when(orderPaymentService.pay(eq(80001L), any(PayOrderRequest.class))).thenReturn(statusVO);

        mockMvc.perform(post("/api/order/{orderId}/pay", 80001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.pickupNo").value("A-01"));

        verify(orderPaymentService).pay(eq(80001L), any(PayOrderRequest.class));
    }

    @Test
    void getOrderStatus_ReturnsStatus() throws Exception {
        OrderStatusVO statusVO = new OrderStatusVO(80001L, "ORD-2026-0001", "PREPARING", "A-01", true);

        when(orderQueryService.status(80001L)).thenReturn(statusVO);

        mockMvc.perform(get("/api/order/{orderId}/status", 80001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PREPARING"));

        verify(orderQueryService).status(80001L);
    }
}
