package cn.zhongjiating.canteen.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PayOrderRequest(
        @NotBlank(message = "支付密码不能为空")
        @Size(min = 6, message = "支付密码长度不能小于6位")
        String payPassword
) {
}
