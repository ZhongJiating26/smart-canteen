package cn.zhongjiating.canteen.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "每日菜单菜品项")
public record DailyMenuItemRequest(
        @Schema(description = "菜品ID", example = "5000001")
        @NotNull(message = "菜单菜品不能为空")
        Long dishId,

        @Schema(description = "每日库存", example = "50")
        @NotNull(message = "每日库存不能为空")
        @Min(value = 0, message = "每日库存不能小于0")
        Integer dailyStock,

        @Schema(description = "库存预警阈值", example = "10")
        @NotNull(message = "预警阈值不能为空")
        @Min(value = 0, message = "预警阈值不能小于0")
        Integer alertThreshold,

        @Schema(description = "1=上架，0=下架", example = "1")
        @NotNull(message = "菜单上架状态不能为空")
        @Min(value = 0, message = "菜单上架状态不正确")
        @Max(value = 1, message = "菜单上架状态不正确")
        Integer isListed
) {
}
