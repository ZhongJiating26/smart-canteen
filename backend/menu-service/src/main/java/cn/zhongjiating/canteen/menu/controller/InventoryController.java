package cn.zhongjiating.canteen.menu.controller;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.menu.dto.InventoryRequest;
import cn.zhongjiating.canteen.menu.service.InventoryService;
import cn.zhongjiating.canteen.menu.vo.InventoryAlertVO;
import cn.zhongjiating.canteen.menu.vo.InventoryResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "菜单库存", description = "库存预占、恢复、消费与商家低库存查询")
@RestController
@RequestMapping("/api/menu/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "预占库存", description = "order-service 创建订单时调用")
    @PostMapping("/reserve")
    public Result<InventoryResultVO> reserve(@Valid @RequestBody InventoryRequest request) {
        return Result.success(inventoryService.reserve(request));
    }

    @Operation(summary = "恢复库存", description = "取消、拒单或超时时调用，按订单库存日志幂等")
    @PostMapping("/release")
    public Result<InventoryResultVO> release(@Valid @RequestBody InventoryRequest request) {
        return Result.success(inventoryService.release(request));
    }

    @Operation(summary = "确认消费库存", description = "核销完成后调用，按订单库存日志幂等")
    @PostMapping("/consume")
    public Result<InventoryResultVO> consume(@Valid @RequestBody InventoryRequest request) {
        return Result.success(inventoryService.consume(request));
    }

    @Operation(summary = "查询当前商家库存预警")
    @GetMapping("/alerts")
    public Result<List<InventoryAlertVO>> alerts() {
        return Result.success(inventoryService.alerts());
    }
}
