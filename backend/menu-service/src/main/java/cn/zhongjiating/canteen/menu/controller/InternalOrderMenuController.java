package cn.zhongjiating.canteen.menu.controller;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.menu.dto.OrderMenuSnapshotRequest;
import cn.zhongjiating.canteen.menu.service.OrderMenuSnapshotService;
import cn.zhongjiating.canteen.menu.vo.OrderMenuSnapshotVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "菜单下单快照内部接口", description = "order-service 创建订单前获取可售菜品快照")
@RestController
@RequestMapping("/internal/menu/order")
public class InternalOrderMenuController {

    private final OrderMenuSnapshotService orderMenuSnapshotService;

    public InternalOrderMenuController(OrderMenuSnapshotService orderMenuSnapshotService) {
        this.orderMenuSnapshotService = orderMenuSnapshotService;
    }

    @Operation(summary = "获取下单菜品快照")
    @PostMapping("/snapshot")
    public Result<OrderMenuSnapshotVO> snapshot(@Valid @RequestBody OrderMenuSnapshotRequest request) {
        return Result.success(orderMenuSnapshotService.snapshot(request));
    }
}
