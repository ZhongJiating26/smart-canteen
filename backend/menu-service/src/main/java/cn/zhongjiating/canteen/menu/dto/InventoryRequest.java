package cn.zhongjiating.canteen.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "库存预占、释放或消费请求")
public record InventoryRequest(
        @Schema(description = "订单ID", example = "8000001")
        @NotNull(message = "订单ID不能为空")
        Long orderId,

        @Schema(description = "库存项")
        @NotEmpty(message = "库存项不能为空")
        List<@Valid InventoryItemRequest> items
) {
}
