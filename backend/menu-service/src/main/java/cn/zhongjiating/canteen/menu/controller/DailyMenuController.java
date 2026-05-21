package cn.zhongjiating.canteen.menu.controller;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.menu.dto.SaveDailyMenuRequest;
import cn.zhongjiating.canteen.menu.dto.UpdateDailyMenuItemsRequest;
import cn.zhongjiating.canteen.menu.service.DailyMenuService;
import cn.zhongjiating.canteen.menu.vo.DailyMenuVO;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

@Tag(name = "每日菜单", description = "商家维护自己售卖时段的可重复每日菜单配置")
@RestController
@RequestMapping("/api/menu/daily-menus")
public class DailyMenuController {

    private final DailyMenuService dailyMenuService;

    public DailyMenuController(DailyMenuService dailyMenuService) {
        this.dailyMenuService = dailyMenuService;
    }

    @Operation(summary = "创建或保存每日菜单")
    @PostMapping
    public Result<DailyMenuVO> save(@Valid @RequestBody SaveDailyMenuRequest request) {
        return Result.success(dailyMenuService.save(request));
    }

    @Operation(summary = "查询当前商家每日菜单")
    @GetMapping
    public Result<List<DailyMenuVO>> list(@RequestParam(required = false) Long merchantId,
                                          @RequestParam(required = false) String mealPeriod,
                                          @RequestParam(required = false) Integer status) {
        return Result.success(dailyMenuService.list(merchantId, mealPeriod, status));
    }

    @Operation(summary = "修改每日菜单菜品项")
    @PutMapping("/{menuId}/items")
    public Result<DailyMenuVO> updateItems(@PathVariable Long menuId,
                                           @Valid @RequestBody UpdateDailyMenuItemsRequest request) {
        return Result.success(dailyMenuService.updateItems(menuId, request));
    }
}
