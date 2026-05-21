package cn.zhongjiating.canteen.pickup.dto;

import jakarta.validation.constraints.NotNull;

public record CallPickupRequest(
        @NotNull(message = "商家窗口 ID 不能为空") Long merchantId,
        @NotNull(message = "订单 ID 不能为空") Long orderId
) {
}
