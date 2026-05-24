package cn.zhongjiating.canteen.order.dto;

import java.math.BigDecimal;

public record MenuItemSnapshotResponse(
        Long dailyMenuItemId,
        Long dishId,
        String dishName,
        byte[] dishImage,
        BigDecimal unitPrice
) {
}
