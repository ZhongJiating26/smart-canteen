package cn.zhongjiating.canteen.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "密码登录请求")
public record PasswordLoginRequest(
        @Schema(description = "用户名，目前为手机号", example = "13800138000")
        @NotBlank(message = "用户名不能为空")
        String username,

        @Schema(description = "登录密码", example = "123456")
        @NotBlank(message = "密码不能为空")
        String password
) {
}
