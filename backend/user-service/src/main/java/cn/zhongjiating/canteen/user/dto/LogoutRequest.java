package cn.zhongjiating.canteen.user.dto;

public record LogoutRequest(
        String accessToken,
        String refreshToken
) {
}
