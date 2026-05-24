package cn.zhongjiating.canteen.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PayPasswordVerifyRequest(
        @NotNull(message = "用户ID不能为空") Long userId,
        @NotBlank(message = "支付密码不能为空") String payPassword
) {
}
