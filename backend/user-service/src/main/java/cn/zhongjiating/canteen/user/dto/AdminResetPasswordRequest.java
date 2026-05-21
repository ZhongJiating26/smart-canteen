package cn.zhongjiating.canteen.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "管理员重置用户密码请求")
public record AdminResetPasswordRequest(
        @Schema(description = "新登录密码", example = "newPass123")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度必须为6-64位")
        String password
) {
}
