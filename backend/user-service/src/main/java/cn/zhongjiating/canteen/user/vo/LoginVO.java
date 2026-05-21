package cn.zhongjiating.canteen.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录响应")
public record LoginVO(
        @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,
        @Schema(description = "用户信息")
        UserInfoVO userInfo
) {
}
