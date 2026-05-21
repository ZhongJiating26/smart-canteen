package cn.zhongjiating.canteen.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登出请求")
public record LogoutRequest(
        @Schema(description = "当前访问令牌；也可通过 Authorization Header 传递", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "刷新令牌；传入后会同步置为失效", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken
) {
}
