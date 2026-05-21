package cn.zhongjiating.canteen.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "内部创建商家账号请求")
public record CreateMerchantAccountRequest(
        @Schema(description = "商家登录手机号，同时作为 username", example = "13800138002")
        @NotBlank(message = "商家登录手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "商家登录手机号格式不正确")
        String phone,

        @Schema(description = "商家登录密码", example = "123456")
        @NotBlank(message = "商家登录密码不能为空")
        @Size(min = 6, max = 64, message = "商家登录密码长度必须为6-64位")
        String password,

        @Schema(description = "账号昵称，默认使用商家窗口名", example = "一号窗口")
        @Size(max = 50, message = "账号昵称长度不能超过50")
        String nickname
) {
}
