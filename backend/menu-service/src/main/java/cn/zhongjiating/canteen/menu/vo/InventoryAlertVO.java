package cn.zhongjiating.canteen.menu.vo;

import java.math.BigDecimal;

public record InventoryAlertVO(
        Long dailyMenuItemId,
        Long dishId,
        String dishName,
        BigDecimal price,
        Integer remainingStock,
        Integer alertThreshold
) {
}
