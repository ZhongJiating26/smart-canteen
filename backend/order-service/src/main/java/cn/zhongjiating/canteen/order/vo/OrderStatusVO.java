package cn.zhongjiating.canteen.order.vo;

import cn.zhongjiating.canteen.order.entity.Order;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record OrderStatusVO(
        @JsonSerialize(using = ToStringSerializer.class)
        Long orderId,
        String orderNo,
        String status,
        String pickupNo,
        boolean hasQrcode
) {
    public static OrderStatusVO from(Order order) {
        return new OrderStatusVO(order.getId(), order.getOrderNo(), order.getStatus(), order.getPickupNo(),
                order.getPickupQrcodeId() != null);
    }
}
