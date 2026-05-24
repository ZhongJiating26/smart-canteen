package cn.zhongjiating.canteen.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MenuSnapshotRequest(
        @NotNull(message = "商家窗口ID不能为空") Long merchantId,
        @NotEmpty(message = "下单菜品不能为空") List<@Valid InventoryItemRequest> items
) {
}
