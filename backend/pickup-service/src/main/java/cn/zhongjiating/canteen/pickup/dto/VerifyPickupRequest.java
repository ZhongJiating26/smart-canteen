package cn.zhongjiating.canteen.pickup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyPickupRequest(
        @NotNull(message = "商家窗口 ID 不能为空") Long merchantId,
        String qrcodeContent,
        String pickupCode,
        @NotBlank(message = "核销方式不能为空") String verifyType
) {
}
