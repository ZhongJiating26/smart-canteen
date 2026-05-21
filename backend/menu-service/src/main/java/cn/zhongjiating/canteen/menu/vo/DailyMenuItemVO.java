package cn.zhongjiating.canteen.menu.vo;

import cn.zhongjiating.canteen.menu.entity.DailyMenuItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "每日菜单项")
public record DailyMenuItemVO(
        Long dailyMenuItemId,
        Long dishId,
        Integer dailyStock,
        Integer totalStock,
        Integer remainingStock,
        Integer reservedStock,
        Integer alertThreshold,
        Integer isListed
) {

    public static DailyMenuItemVO from(DailyMenuItem item) {
        return new DailyMenuItemVO(item.getId(), item.getDishId(), item.getDailyStock(), item.getTotalStock(),
                item.getRemainingStock(), item.getReservedStock(), item.getAlertThreshold(), item.getIsListed());
    }
}
