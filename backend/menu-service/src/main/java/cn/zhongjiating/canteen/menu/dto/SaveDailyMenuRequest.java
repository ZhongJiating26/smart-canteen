package cn.zhongjiating.canteen.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "每日菜单配置请求")
public record SaveDailyMenuRequest(
        @Schema(description = "商家ID。当前范围内商家即窗口", example = "2000001")
        @NotNull(message = "商家不能为空")
        Long merchantId,

        @Schema(description = "售卖时段", example = "LUNCH")
        @NotBlank(message = "售卖时段不能为空")
        String mealPeriod,

        @Schema(description = "1=启用，0=草稿", example = "1")
        @Min(value = 0, message = "菜单状态不正确")
        @Max(value = 1, message = "菜单状态不正确")
        Integer status,

        @Schema(description = "菜单项")
        @NotEmpty(message = "菜单菜品不能为空")
        List<@Valid DailyMenuItemRequest> items
) {
}
