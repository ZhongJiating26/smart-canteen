package cn.zhongjiating.canteen.menu.vo;

import cn.zhongjiating.canteen.menu.entity.MenuConfig;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "菜单服务配置")
public record MenuConfigVO(
        String configKey,
        String configValue,
        String configType,
        String description,
        LocalDateTime updateTime,
        Long updatedBy
) {

    public static MenuConfigVO from(MenuConfig config) {
        return new MenuConfigVO(config.getConfigKey(), config.getConfigValue(), config.getConfigType(),
                config.getDescription(), config.getUpdateTime(), config.getUpdatedBy());
    }
}
