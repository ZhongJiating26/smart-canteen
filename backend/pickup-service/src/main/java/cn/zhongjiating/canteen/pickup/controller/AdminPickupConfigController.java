package cn.zhongjiating.canteen.pickup.controller;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.pickup.dto.UpdatePickupConfigRequest;
import cn.zhongjiating.canteen.pickup.service.PickupConfigService;
import cn.zhongjiating.canteen.pickup.vo.PickupConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员取餐配置", description = "取餐服务运行配置")
@RestController
@RequestMapping("/api/admin/pickup/config")
public class AdminPickupConfigController {

    private final PickupConfigService pickupConfigService;

    public AdminPickupConfigController(PickupConfigService pickupConfigService) {
        this.pickupConfigService = pickupConfigService;
    }

    @Operation(summary = "查询取餐服务配置")
    @GetMapping
    public Result<List<PickupConfigVO>> list() {
        requireAdmin();
        return Result.success(pickupConfigService.list());
    }

    @Operation(summary = "修改取餐服务配置")
    @PutMapping
    public Result<PickupConfigVO> update(@Valid @RequestBody UpdatePickupConfigRequest request) {
        UserContext context = requireAdmin();
        return Result.success(pickupConfigService.update(context.getUserId(), request));
    }

    private UserContext requireAdmin() {
        UserContext context = UserContextHolder.require();
        if (!context.isAdmin()) {
            throw new ForbiddenException();
        }
        return context;
    }
}
