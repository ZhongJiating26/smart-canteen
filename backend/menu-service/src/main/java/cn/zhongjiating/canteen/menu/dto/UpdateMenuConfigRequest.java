package cn.zhongjiating.canteen.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "菜单服务配置修改请求")
public record UpdateMenuConfigRequest(
        @Schema(description = "配置键", example = "stock_alert_scan_interval")
        @NotBlank(message = "配置键不能为空")
        @Size(max = 50, message = "配置键长度不能超过50")
        String configKey,

        @Schema(description = "配置值", example = "300")
        @NotBlank(message = "配置值不能为空")
        @Size(max = 255, message = "配置值长度不能超过255")
        String configValue
) {
}
