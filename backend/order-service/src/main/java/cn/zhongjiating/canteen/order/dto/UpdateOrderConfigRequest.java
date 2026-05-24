package cn.zhongjiating.canteen.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "订单服务配置修改请求")
public record UpdateOrderConfigRequest(
        @Schema(description = "配置键", example = "pay_timeout_minutes")
        @NotBlank(message = "配置键不能为空")
        @Size(max = 50, message = "配置键长度不能超过50")
        String configKey,

        @Schema(description = "配置值", example = "10")
        @NotBlank(message = "配置值不能为空")
        @Size(max = 255, message = "配置值长度不能超过255")
        String configValue
) {
}
