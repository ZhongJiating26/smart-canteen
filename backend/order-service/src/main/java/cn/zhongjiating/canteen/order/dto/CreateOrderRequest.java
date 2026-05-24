package cn.zhongjiating.canteen.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "商家窗口ID不能为空") Long merchantId,
        @Size(max = 200, message = "订单备注不能超过200字") String remark,
        @NotEmpty(message = "下单菜品不能为空") List<@Valid CreateOrderItemRequest> items
) {
}
