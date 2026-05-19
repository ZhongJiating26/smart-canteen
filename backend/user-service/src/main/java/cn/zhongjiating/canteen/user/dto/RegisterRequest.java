package cn.zhongjiating.canteen.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^(1\\d{10}|[A-Za-z0-9]{5,20})$", message = "用户名必须是手机号或5-20位学工号")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度必须为6-64位")
        String password,

        @NotBlank(message = "支付密码不能为空")
        @Size(min = 6, max = 32, message = "支付密码长度必须为6-32位")
        String payPassword,

        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{4,6}$", message = "验证码必须是4-6位数字")
        String verifyCode,

        @Size(max = 50, message = "昵称最多50个字符")
        String nickname
) {
}
