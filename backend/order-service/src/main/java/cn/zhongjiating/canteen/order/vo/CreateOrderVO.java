package cn.zhongjiating.canteen.order.vo;

import cn.zhongjiating.canteen.order.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateOrderVO(
        Long orderId,
        String orderNo,
        BigDecimal totalAmount,
        String status,
        LocalDateTime expireTime
) {
    public static CreateOrderVO from(Order order, int payTimeoutMinutes) {
        return new CreateOrderVO(order.getId(), order.getOrderNo(), order.getTotalAmount(), order.getStatus(),
                order.getCreateTime().plusMinutes(payTimeoutMinutes));
    }
}
