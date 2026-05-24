package cn.zhongjiating.canteen.order.controller;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.order.service.OrderMerchantService;
import cn.zhongjiating.canteen.order.service.OrderQueryService;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import cn.zhongjiating.canteen.order.vo.OrderSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "商家订单", description = "商家按窗口查询待处理订单")
@RestController
@RequestMapping("/api/order/merchant")
public class MerchantOrderController {

    private final OrderQueryService orderQueryService;
    private final OrderMerchantService orderMerchantService;

    public MerchantOrderController(OrderQueryService orderQueryService, OrderMerchantService orderMerchantService) {
        this.orderQueryService = orderQueryService;
        this.orderMerchantService = orderMerchantService;
    }

    @Operation(summary = "查询商家订单列表")
    @GetMapping("/list")
    public Result<List<OrderSummaryVO>> list(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) Long merchantId) {
        return Result.success(orderQueryService.merchantOrders(status, merchantId));
    }

    @Operation(summary = "商家接单")
    @PostMapping("/{orderId}/accept")
    public Result<OrderStatusVO> accept(@PathVariable Long orderId) {
        return Result.success(orderMerchantService.accept(orderId));
    }

    @Operation(summary = "商家拒单")
    @PostMapping("/{orderId}/reject")
    public Result<OrderStatusVO> reject(@PathVariable Long orderId) {
        return Result.success(orderMerchantService.reject(orderId));
    }

    @Operation(summary = "商家出餐")
    @PostMapping("/{orderId}/ready")
    public Result<OrderStatusVO> ready(@PathVariable Long orderId) {
        return Result.success(orderMerchantService.ready(orderId));
    }
}
