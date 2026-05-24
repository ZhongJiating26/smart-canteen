package cn.zhongjiating.canteen.order.vo;

import cn.zhongjiating.canteen.order.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemVO(
        Long dishId,
        String dishName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
    public static OrderItemVO from(OrderItem item) {
        return new OrderItemVO(item.getDishId(), item.getDishName(), item.getUnitPrice(), item.getQuantity(),
                item.getSubtotal());
    }
}
