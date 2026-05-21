package cn.zhongjiating.canteen.menu.controller;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.menu.dto.UpdateMenuConfigRequest;
import cn.zhongjiating.canteen.menu.service.MenuConfigService;
import cn.zhongjiating.canteen.menu.vo.MenuConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员菜单配置", description = "菜单服务运行配置")
@RestController
@RequestMapping("/api/admin/menu/config")
public class AdminMenuConfigController {

    private final MenuConfigService menuConfigService;

    public AdminMenuConfigController(MenuConfigService menuConfigService) {
        this.menuConfigService = menuConfigService;
    }

    @Operation(summary = "查询菜单服务配置", description = "从菜单服务配置表读取并刷新本服务 Redis 配置缓存")
    @GetMapping
    public Result<List<MenuConfigVO>> list() {
        requireAdmin();
        return Result.success(menuConfigService.list());
    }

    @Operation(summary = "修改菜单服务配置", description = "写入菜单服务配置表并刷新 Redis 缓存")
    @PutMapping
    public Result<MenuConfigVO> update(@Valid @RequestBody UpdateMenuConfigRequest request) {
        UserContext context = requireAdmin();
        return Result.success(menuConfigService.update(context.getUserId(), request));
    }

    private UserContext requireAdmin() {
        UserContext context = UserContextHolder.require();
        if (!context.isAdmin()) {
            throw new ForbiddenException();
        }
        return context;
    }
}
