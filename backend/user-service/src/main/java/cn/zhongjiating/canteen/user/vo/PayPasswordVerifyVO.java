package cn.zhongjiating.canteen.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "支付密码验证响应")
public record PayPasswordVerifyVO(
        @Schema(description = "是否验证通过", example = "true")
        boolean valid
) {
}
