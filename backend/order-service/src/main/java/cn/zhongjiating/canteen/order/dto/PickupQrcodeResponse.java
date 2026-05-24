package cn.zhongjiating.canteen.order.dto;

import java.time.LocalDateTime;

public record PickupQrcodeResponse(
        Long qrcodeId,
        Long orderId,
        String pickupNo,
        String pickupCode,
        String qrcodeContent,
        Boolean isVerified,
        LocalDateTime createTime
) {
}
