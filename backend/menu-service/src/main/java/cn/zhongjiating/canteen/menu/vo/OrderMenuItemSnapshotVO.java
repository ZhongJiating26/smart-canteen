package cn.zhongjiating.canteen.menu.vo;

import cn.zhongjiating.canteen.menu.entity.DailyMenuItem;
import cn.zhongjiating.canteen.menu.entity.Dish;

import java.math.BigDecimal;

public record OrderMenuItemSnapshotVO(
        Long dailyMenuItemId,
        Long dishId,
        String dishName,
        byte[] dishImage,
        BigDecimal unitPrice
) {
    public static OrderMenuItemSnapshotVO from(DailyMenuItem item, Dish dish) {
        return new OrderMenuItemSnapshotVO(item.getId(), dish.getId(), dish.getName(), dish.getImage(),
                dish.getPrice());
    }
}
