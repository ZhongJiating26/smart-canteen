package cn.zhongjiating.canteen.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "验证码登录请求")
public record CodeLoginRequest(
        @Schema(description = "手机号", example = "13800138000")
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
        String phone,

        @Schema(description = "验证码", example = "123456")
        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{4,6}$", message = "验证码必须是4-6位数字")
        String verifyCode
) {
}
