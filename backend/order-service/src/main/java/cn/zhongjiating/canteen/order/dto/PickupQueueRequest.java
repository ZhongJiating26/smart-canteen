package cn.zhongjiating.canteen.order.dto;

import jakarta.validation.constraints.NotNull;

public record PickupQueueRequest(
        @NotNull(message = "商家窗口ID不能为空") Long merchantId,
        @NotNull(message = "订单ID不能为空") Long orderId
) {
}
