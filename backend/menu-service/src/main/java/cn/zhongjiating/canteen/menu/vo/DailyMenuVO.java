package cn.zhongjiating.canteen.menu.vo;

import cn.zhongjiating.canteen.menu.entity.DailyMenu;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "每日菜单配置")
public record DailyMenuVO(Long menuId, Long merchantId, String mealPeriod, Integer status, List<DailyMenuItemVO> items) {

    public static DailyMenuVO of(DailyMenu menu, List<DailyMenuItemVO> items) {
        return new DailyMenuVO(menu.getId(), menu.getMerchantId(), menu.getMealPeriod(), menu.getStatus(), items);
    }
}
