package cn.zhongjiating.canteen.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "菜品分类请求")
public record CategoryRequest(
        @Schema(description = "分类名称", example = "主食")
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 50, message = "分类名称长度不能超过50")
        String name,

        @Schema(description = "排序值，越小越前", example = "1")
        @NotNull(message = "分类排序不能为空")
        Integer sort
) {
}
