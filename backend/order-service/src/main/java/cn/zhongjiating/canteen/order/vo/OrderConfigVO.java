package cn.zhongjiating.canteen.order.vo;

import cn.zhongjiating.canteen.order.entity.OrderConfig;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "订单服务配置")
public record OrderConfigVO(
        String configKey,
        String configValue,
        String configType,
        String description,
        LocalDateTime updateTime,
        Long updatedBy
) {

    public static OrderConfigVO from(OrderConfig config) {
        return new OrderConfigVO(config.getConfigKey(), config.getConfigValue(), config.getConfigType(),
                config.getDescription(), config.getUpdateTime(), config.getUpdatedBy());
    }
}
