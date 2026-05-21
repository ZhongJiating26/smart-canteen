package cn.zhongjiating.canteen.menu.controller;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.menu.dto.AdminMerchantCreateRequest;
import cn.zhongjiating.canteen.menu.dto.AdminMerchantUpdateRequest;
import cn.zhongjiating.canteen.menu.service.AdminMerchantService;
import cn.zhongjiating.canteen.menu.vo.MerchantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员商家窗口管理", description = "管理员创建和维护商家窗口")
@RestController
@RequestMapping("/api/admin/merchants")
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    public AdminMerchantController(AdminMerchantService adminMerchantService) {
        this.adminMerchantService = adminMerchantService;
    }

    @Operation(summary = "分页查询商家窗口")
    @GetMapping
    public Result<PageResult<MerchantVO>> page(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Long page,
            @Parameter(description = "每页条数，最大100", example = "10")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "商家窗口名称关键字", example = "一号")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "状态：1=启用，0=停用", example = "1")
            @RequestParam(required = false) Integer status) {
        requireAdmin();
        return Result.success(adminMerchantService.page(page, size, keyword, status));
    }

    @Operation(summary = "新增商家窗口", description = "先在 user-service 创建 MERCHANT 账号，再创建商家窗口记录")
    @PostMapping
    public Result<MerchantVO> create(@Valid @RequestBody AdminMerchantCreateRequest request) {
        requireAdmin();
        return Result.success(adminMerchantService.create(request));
    }

    @Operation(summary = "修改商家窗口", description = "修改窗口资料和启停状态")
    @PutMapping("/{merchantId}")
    public Result<MerchantVO> update(
            @Parameter(description = "商家窗口ID", example = "2000001")
            @PathVariable Long merchantId,
            @Valid @RequestBody AdminMerchantUpdateRequest request) {
        requireAdmin();
        return Result.success(adminMerchantService.update(merchantId, request));
    }

    private UserContext requireAdmin() {
        UserContext context = UserContextHolder.require();
        if (!context.isAdmin()) {
            throw new ForbiddenException();
        }
        return context;
    }
}
