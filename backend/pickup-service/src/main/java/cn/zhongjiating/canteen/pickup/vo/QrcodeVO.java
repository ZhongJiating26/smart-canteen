package cn.zhongjiating.canteen.pickup.vo;

import cn.zhongjiating.canteen.pickup.entity.PickupQrcode;

import java.time.LocalDateTime;

public record QrcodeVO(
        Long qrcodeId,
        Long orderId,
        String pickupNo,
        String pickupCode,
        String qrcodeContent,
        Boolean isVerified,
        LocalDateTime createTime
) {
    public static QrcodeVO from(PickupQrcode qrcode, String pickupNo) {
        return new QrcodeVO(qrcode.getId(), qrcode.getOrderId(), pickupNo, qrcode.getPickupCode(), qrcode.getQrcodeContent(),
                qrcode.getIsVerified() == 1, qrcode.getCreateTime());
    }
}
