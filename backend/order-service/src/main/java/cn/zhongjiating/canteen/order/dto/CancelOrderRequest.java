package cn.zhongjiating.canteen.order.dto;

import jakarta.validation.constraints.Size;

public record CancelOrderRequest(
        @Size(max = 200, message = "取消原因长度不能超过200")
        String reason
) {
}
