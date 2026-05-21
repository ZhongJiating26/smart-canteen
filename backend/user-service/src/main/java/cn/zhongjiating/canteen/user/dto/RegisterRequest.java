package cn.zhongjiating.canteen.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "用户注册请求")
public record RegisterRequest(
        @Schema(description = "手机号，注册后同时作为 username", example = "13800138000")
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
        String phone,

        @Schema(description = "学工号，可为空", example = "20260001")
        @Pattern(regexp = "^$|^[A-Za-z0-9]{5,20}$", message = "学工号必须是5-20位字母或数字")
        String studentNo,

        @Schema(description = "登录密码", example = "123456")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度必须为6-64位")
        String password,

        @Schema(description = "支付密码，课程项目中明文模拟存储", example = "123456")
        @NotBlank(message = "支付密码不能为空")
        @Size(min = 6, max = 32, message = "支付密码长度必须为6-32位")
        String payPassword,

        @Schema(description = "手机号验证码，课程项目固定为 123456", example = "123456")
        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{4,6}$", message = "验证码必须是4-6位数字")
        String verifyCode,

        @Schema(description = "昵称", example = "张三")
        @Size(max = 50, message = "昵称最多50个字符")
        String nickname
) {
}
