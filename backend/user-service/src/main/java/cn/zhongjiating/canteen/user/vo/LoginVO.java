package cn.zhongjiating.canteen.user.vo;

public record LoginVO(
        String accessToken,
        String refreshToken,
        UserInfoVO userInfo
) {
}
