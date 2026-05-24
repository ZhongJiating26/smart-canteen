package cn.zhongjiating.canteen.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InventoryOperationRequest(
        @NotNull(message = "订单ID不能为空") Long orderId,
        @NotEmpty(message = "库存项不能为空") List<@Valid InventoryItemRequest> items
) {
}
