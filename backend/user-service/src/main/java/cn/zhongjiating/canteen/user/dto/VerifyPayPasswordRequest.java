package cn.zhongjiating.canteen.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "支付密码验证请求")
public record VerifyPayPasswordRequest(
        @Schema(description = "用户ID", example = "1000001")
        @NotNull(message = "用户ID不能为空")
        Long userId,

        @Schema(description = "支付密码", example = "123456")
        @NotBlank(message = "支付密码不能为空")
        String payPassword
) {
}
