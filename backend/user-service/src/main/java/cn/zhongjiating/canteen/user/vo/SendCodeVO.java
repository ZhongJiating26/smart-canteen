package cn.zhongjiating.canteen.user.vo;

public record SendCodeVO(
        String phone,
        String verifyCode
) {
}
