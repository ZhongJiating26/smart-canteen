package cn.zhongjiating.canteen.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "发送验证码请求")
public record SendCodeRequest(
        @Schema(description = "手机号", example = "13800138000")
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
        String phone
) {
}
