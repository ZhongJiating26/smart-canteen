package cn.zhongjiating.canteen.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryItemRequest(
        @NotNull(message = "每日菜单项ID不能为空") Long dailyMenuItemId,
        @NotNull(message = "库存数量不能为空") @Min(value = 1, message = "库存数量必须大于0") Integer quantity
) {
}
