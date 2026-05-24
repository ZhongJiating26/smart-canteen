package cn.zhongjiating.canteen.order.vo;

import cn.zhongjiating.canteen.order.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryVO(
        Long orderId,
        String orderNo,
        Long merchantId,
        BigDecimal totalAmount,
        String status,
        String pickupNo,
        LocalDateTime createTime
) {
    public static OrderSummaryVO from(Order order) {
        return new OrderSummaryVO(order.getId(), order.getOrderNo(), order.getMerchantId(), order.getTotalAmount(),
                order.getStatus(), order.getPickupNo(), order.getCreateTime());
    }
}
