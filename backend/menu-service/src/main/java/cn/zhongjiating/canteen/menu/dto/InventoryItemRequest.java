package cn.zhongjiating.canteen.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "库存变更项")
public record InventoryItemRequest(
        @Schema(description = "每日菜单项ID", example = "7000001")
        @NotNull(message = "每日菜单项不能为空")
        Long dailyMenuItemId,

        @Schema(description = "数量", example = "2")
        @NotNull(message = "库存数量不能为空")
        @Min(value = 1, message = "库存数量必须大于0")
        Integer quantity
) {
}
