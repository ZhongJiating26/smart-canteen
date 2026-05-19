package cn.zhongjiating.canteen.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyPayPasswordRequest(
        @NotNull(message = "用户ID不能为空")
        Long userId,

        @NotBlank(message = "支付密码不能为空")
        String payPassword
) {
}
