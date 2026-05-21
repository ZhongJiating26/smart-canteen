package cn.zhongjiating.canteen.user.controller;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.user.dto.AdminResetPasswordRequest;
import cn.zhongjiating.canteen.user.dto.AdminUserStatusRequest;
import cn.zhongjiating.canteen.user.service.AdminUserService;
import cn.zhongjiating.canteen.user.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员用户管理", description = "管理员维护用户列表、状态和密码")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "分页查询用户", description = "按角色、状态、关键字筛选用户")
    @GetMapping
    public Result<PageResult<UserInfoVO>> pageUsers(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "每页条数，最大100", example = "10")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "角色：USER/MERCHANT/ADMIN", example = "USER")
            @RequestParam(required = false) String role,
            @Parameter(description = "状态：1=启用，0=禁用", example = "1")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "用户名、手机号、学工号或昵称关键字", example = "138")
            @RequestParam(required = false) String keyword) {
        requireAdmin();
        return Result.success(adminUserService.pageUsers(page, size, role, status, keyword));
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{userId}")
    public Result<UserInfoVO> getUser(
            @Parameter(description = "用户ID", example = "1000001")
            @PathVariable Long userId) {
        requireAdmin();
        return Result.success(adminUserService.getUser(userId));
    }

    @Operation(summary = "修改用户状态", description = "启用或禁用用户，管理员不能禁用自己")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "修改成功",
            content = @Content(examples = @ExampleObject(value = "{\"code\":200,\"message\":\"success\",\"data\":{\"userId\":1000001,\"status\":0}}")))
    @PutMapping("/{userId}/status")
    public Result<UserInfoVO> updateStatus(
            @Parameter(description = "用户ID", example = "1000001")
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserStatusRequest request) {
        UserContext context = requireAdmin();
        return Result.success(adminUserService.updateStatus(context.getUserId(), userId, request));
    }

    @Operation(summary = "重置用户密码", description = "管理员将指定用户登录密码重置为新密码")
    @PutMapping("/{userId}/password")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID", example = "1000001")
            @PathVariable Long userId,
            @Valid @RequestBody AdminResetPasswordRequest request) {
        requireAdmin();
        adminUserService.resetPassword(userId, request);
        return Result.success();
    }

    private UserContext requireAdmin() {
        UserContext context = UserContextHolder.require();
        if (!context.isAdmin()) {
            throw new ForbiddenException();
        }
        return context;
    }
}
