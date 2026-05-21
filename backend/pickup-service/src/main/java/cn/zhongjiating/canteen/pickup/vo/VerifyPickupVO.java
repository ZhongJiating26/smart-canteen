package cn.zhongjiating.canteen.pickup.vo;

import java.time.LocalDateTime;

public record VerifyPickupVO(Long orderId, Long qrcodeId, LocalDateTime verifyTime) {
}
