package cn.zhongjiating.canteen.order.controller;

import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.order.dto.CreateOrderRequest;
import cn.zhongjiating.canteen.order.dto.CancelOrderRequest;
import cn.zhongjiating.canteen.order.dto.PayOrderRequest;
import cn.zhongjiating.canteen.order.service.OrderCancelService;
import cn.zhongjiating.canteen.order.service.OrderCreateService;
import cn.zhongjiating.canteen.order.service.OrderPaymentService;
import cn.zhongjiating.canteen.order.service.OrderQueryService;
import cn.zhongjiating.canteen.order.vo.CreateOrderVO;
import cn.zhongjiating.canteen.order.vo.OrderDetailVO;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import cn.zhongjiating.canteen.order.vo.OrderSummaryVO;
import cn.zhongjiating.canteen.order.vo.UserOrderStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单查询", description = "用户订单列表、轮询状态和详情")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderQueryService orderQueryService;
    private final OrderCreateService orderCreateService;
    private final OrderPaymentService orderPaymentService;
    private final OrderCancelService orderCancelService;

    public OrderController(OrderQueryService orderQueryService, OrderCreateService orderCreateService,
                           OrderPaymentService orderPaymentService, OrderCancelService orderCancelService) {
        this.orderQueryService = orderQueryService;
        this.orderCreateService = orderCreateService;
        this.orderPaymentService = orderPaymentService;
        this.orderCancelService = orderCancelService;
    }

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<CreateOrderVO> create(@RequestHeader("X-Idempotent-Key") String idempotentKey,
                                        @Valid @RequestBody CreateOrderRequest request) {
        return Result.success(orderCreateService.create(idempotentKey, request));
    }

    @Operation(summary = "支付订单")
    @PostMapping("/{orderId}/pay")
    public Result<OrderStatusVO> pay(@PathVariable Long orderId, @Valid @RequestBody PayOrderRequest request) {
        return Result.success(orderPaymentService.pay(orderId, request));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{orderId}/cancel")
    public Result<OrderStatusVO> cancel(@PathVariable Long orderId,
                                        @Valid @RequestBody(required = false) CancelOrderRequest request) {
        return Result.success(orderCancelService.cancelByUser(orderId, request));
    }

    @Operation(summary = "查询我的订单")
    @GetMapping("/my")
    public Result<PageResult<OrderSummaryVO>> myOrders(@RequestParam(defaultValue = "1") Long page,
                                                       @RequestParam(defaultValue = "20") Long size,
                                                       @RequestParam(required = false) String status) {
        return Result.success(orderQueryService.myOrders(page, size, status));
    }

    @Operation(summary = "查询订单轮询状态")
    @GetMapping("/{orderId}/status")
    public Result<OrderStatusVO> status(@PathVariable Long orderId) {
        return Result.success(orderQueryService.status(orderId));
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{orderId}")
    public Result<OrderDetailVO> detail(@PathVariable Long orderId) {
        return Result.success(orderQueryService.detail(orderId));
    }

    @Operation(summary = "查询我的订单统计数据")
    @GetMapping("/my/statistics")
    public Result<UserOrderStatisticsVO> getMyStatistics() {
        return Result.success(orderQueryService.getMyStatistics());
    }
}
