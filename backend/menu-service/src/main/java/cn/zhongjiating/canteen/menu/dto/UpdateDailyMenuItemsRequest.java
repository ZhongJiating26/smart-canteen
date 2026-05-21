package cn.zhongjiating.canteen.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "每日菜单项修改请求")
public record UpdateDailyMenuItemsRequest(
        @NotEmpty(message = "菜单菜品不能为空")
        List<@Valid DailyMenuItemRequest> items
) {
}
