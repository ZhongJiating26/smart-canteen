package cn.zhongjiating.canteen.pickup.vo;

import cn.zhongjiating.canteen.pickup.entity.CallEvent;

import java.time.LocalDateTime;

public record CallEventVO(Long orderId, String pickupNo, String callType, LocalDateTime callTime) {

    public static CallEventVO from(CallEvent event) {
        return new CallEventVO(event.getOrderId(), event.getPickupNo(), event.getCallType(), event.getCallTime());
    }
}
