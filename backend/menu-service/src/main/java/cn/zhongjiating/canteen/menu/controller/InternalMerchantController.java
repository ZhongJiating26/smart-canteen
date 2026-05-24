package cn.zhongjiating.canteen.menu.controller;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.menu.service.MerchantService;
import cn.zhongjiating.canteen.menu.vo.MerchantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "菜单内部商家接口", description = "业务服务间查询商家窗口归属")
@RestController
@RequestMapping("/internal/menu/merchants")
public class InternalMerchantController {

    private final MerchantService merchantService;

    public InternalMerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Operation(summary = "按商家用户查询窗口")
    @GetMapping("/by-user/{userId}")
    public Result<MerchantVO> byUserId(@PathVariable Long userId) {
        return Result.success(MerchantVO.from(merchantService.requireMerchantByUserId(userId)));
    }
}
