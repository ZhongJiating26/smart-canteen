package cn.zhongjiating.canteen.order.vo;

import cn.zhongjiating.canteen.order.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailVO(
        Long orderId,
        String orderNo,
        Long userId,
        Long merchantId,
        BigDecimal totalAmount,
        String status,
        String pickupNo,
        String remark,
        String cancelReason,
        LocalDateTime payTime,
        LocalDateTime acceptTime,
        LocalDateTime completeTime,
        LocalDateTime cancelTime,
        LocalDateTime createTime,
        List<OrderItemVO> items,
        List<OrderStatusLogVO> statusTimeline
) {
    public static OrderDetailVO from(Order order, List<OrderItemVO> items, List<OrderStatusLogVO> statusTimeline) {
        return new OrderDetailVO(order.getId(), order.getOrderNo(), order.getUserId(), order.getMerchantId(),
                order.getTotalAmount(), order.getStatus(), order.getPickupNo(), order.getRemark(),
                order.getCancelReason(), order.getPayTime(), order.getAcceptTime(), order.getCompleteTime(),
                order.getCancelTime(), order.getCreateTime(), items, statusTimeline);
    }
}
