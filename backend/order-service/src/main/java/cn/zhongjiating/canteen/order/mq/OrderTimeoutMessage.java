package cn.zhongjiating.canteen.order.mq;

import java.time.LocalDateTime;

public record OrderTimeoutMessage(
        Long orderId,
        String orderNo,
        String timeoutType,
        LocalDateTime sendTime
) {
}
