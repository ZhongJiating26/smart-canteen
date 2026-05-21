package cn.zhongjiating.canteen.pickup.vo;

import cn.zhongjiating.canteen.pickup.entity.PickupConfig;

import java.time.LocalDateTime;

public record PickupConfigVO(
        String configKey,
        String configValue,
        String configType,
        String description,
        LocalDateTime updateTime,
        Long updatedBy
) {

    public static PickupConfigVO from(PickupConfig config) {
        return new PickupConfigVO(config.getConfigKey(), config.getConfigValue(), config.getConfigType(),
                config.getDescription(), config.getUpdateTime(), config.getUpdatedBy());
    }
}
