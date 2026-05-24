package cn.zhongjiating.canteen.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
        @NotNull(message = "每日菜单项ID不能为空") Long dailyMenuItemId,
        @NotNull(message = "下单数量不能为空") @Min(value = 1, message = "下单数量必须大于0") Integer quantity
) {
    public InventoryItemRequest toInventoryItem() {
        return new InventoryItemRequest(dailyMenuItemId, quantity);
    }
}
