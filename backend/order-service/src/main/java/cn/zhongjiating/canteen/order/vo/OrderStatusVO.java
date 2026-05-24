package cn.zhongjiating.canteen.order.vo;

import cn.zhongjiating.canteen.order.entity.Order;

public record OrderStatusVO(
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
