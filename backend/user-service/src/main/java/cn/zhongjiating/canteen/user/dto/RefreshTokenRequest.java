package cn.zhongjiating.canteen.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "刷新 Token 请求")
public record RefreshTokenRequest(
        @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "refreshToken不能为空")
        String refreshToken,

        @Schema(description = "旧访问令牌，用于加入黑名单", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken
) {
}
