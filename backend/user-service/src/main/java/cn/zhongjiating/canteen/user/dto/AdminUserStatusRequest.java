package cn.zhongjiating.canteen.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理员修改用户状态请求")
public record AdminUserStatusRequest(
        @Schema(description = "用户状态：1=启用，0=禁用", example = "0")
        @NotNull(message = "状态不能为空")
        @Min(value = 0, message = "状态只能是0或1")
        @Max(value = 1, message = "状态只能是0或1")
        Integer status
) {
}
