package cn.zhongjiating.canteen.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "发送验证码响应")
public record SendCodeVO(
        @Schema(description = "手机号", example = "13800138000")
        String phone,
        @Schema(description = "模拟验证码，课程项目固定为 123456", example = "123456")
        String verifyCode
) {
}
