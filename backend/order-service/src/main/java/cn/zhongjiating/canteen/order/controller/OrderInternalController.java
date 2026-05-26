package cn.zhongjiating.canteen.order.controller;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.order.service.OrderCompleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.zhongjiating.canteen.order.service.OrderQueryService;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Map;

@Tag(name = "订单内部接口", description = "服务间订单状态同步")
@RestController
@RequestMapping("/internal/order")
public class OrderInternalController {

    private final OrderCompleteService orderCompleteService;
    private final OrderQueryService orderQueryService;

    public OrderInternalController(OrderCompleteService orderCompleteService, OrderQueryService orderQueryService) {
        this.orderCompleteService = orderCompleteService;
        this.orderQueryService = orderQueryService;
    }

    @Operation(summary = "核销完成同步订单")
    @PostMapping("/{orderId}/complete")
    public Result<Void> complete(@PathVariable Long orderId) {
        orderCompleteService.complete(orderId);
        return Result.success();
    }

    @Operation(summary = "查询商家月销量")
    @PostMapping("/merchant-sales")
    public Result<Map<Long, Integer>> getMerchantMonthlySales(@RequestBody List<Long> merchantIds) {
        return Result.success(orderQueryService.getMerchantMonthlySales(merchantIds));
    }
}

