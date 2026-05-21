package cn.zhongjiating.canteen.menu.controller;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.menu.service.MenuBrowseService;
import cn.zhongjiating.canteen.menu.vo.AvailableMenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "可售菜单浏览", description = "游客和用户浏览已发布、上架且有库存的菜单")
@RestController
@RequestMapping("/api/menu")
public class MenuBrowseController {

    private final MenuBrowseService menuBrowseService;

    public MenuBrowseController(MenuBrowseService menuBrowseService) {
        this.menuBrowseService = menuBrowseService;
    }

    @Operation(summary = "查询当前可售菜单")
    @GetMapping("/available")
    public Result<AvailableMenuVO> available(@RequestParam(required = false) Long merchantId,
                                             @RequestParam(required = false) String mealPeriod,
                                             @RequestParam(required = false) Long categoryId) {
        return Result.success(menuBrowseService.available(merchantId, mealPeriod, categoryId));
    }
}
