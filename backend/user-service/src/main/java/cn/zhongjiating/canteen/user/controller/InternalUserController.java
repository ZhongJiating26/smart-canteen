package cn.zhongjiating.canteen.user.controller;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.user.dto.CreateMerchantAccountRequest;
import cn.zhongjiating.canteen.user.service.AdminUserService;
import cn.zhongjiating.canteen.user.vo.MerchantAccountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户内部接口", description = "业务服务间调用")
@RestController
@RequestMapping("/internal/user")
public class InternalUserController {

    private final AdminUserService adminUserService;

    public InternalUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "创建商家登录账号", description = "供 menu-service 新增商家窗口时调用")
    @PostMapping("/merchant-accounts")
    public Result<MerchantAccountVO> createMerchantAccount(@Valid @RequestBody CreateMerchantAccountRequest request) {
        return Result.success(adminUserService.createMerchantAccount(request));
    }
}
