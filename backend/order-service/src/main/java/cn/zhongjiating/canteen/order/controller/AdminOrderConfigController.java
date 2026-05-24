package cn.zhongjiating.canteen.order.controller;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.order.dto.UpdateOrderConfigRequest;
import cn.zhongjiating.canteen.order.service.OrderConfigService;
import cn.zhongjiating.canteen.order.vo.OrderConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员订单配置", description = "订单服务运行配置")
@RestController
@RequestMapping("/api/admin/order/config")
public class AdminOrderConfigController {

    private final OrderConfigService orderConfigService;

    public AdminOrderConfigController(OrderConfigService orderConfigService) {
        this.orderConfigService = orderConfigService;
    }

    @Operation(summary = "查询订单服务配置", description = "从订单服务配置表读取并刷新本服务 Redis 配置缓存")
    @GetMapping
    public Result<List<OrderConfigVO>> list() {
        requireAdmin();
        return Result.success(orderConfigService.list());
    }

    @Operation(summary = "修改订单服务配置", description = "写入订单服务配置表并刷新 Redis 缓存")
    @PutMapping
    public Result<OrderConfigVO> update(@Valid @RequestBody UpdateOrderConfigRequest request) {
        UserContext context = requireAdmin();
        return Result.success(orderConfigService.update(context.getUserId(), request));
    }

    private UserContext requireAdmin() {
        UserContext context = UserContextHolder.require();
        if (!context.isAdmin()) {
            throw new ForbiddenException();
        }
        return context;
    }
}
