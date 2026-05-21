package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.menu.dto.UpdateMenuConfigRequest;
import cn.zhongjiating.canteen.menu.entity.MenuConfig;
import cn.zhongjiating.canteen.menu.mapper.MenuConfigMapper;
import cn.zhongjiating.canteen.menu.service.MenuConfigService;
import cn.zhongjiating.canteen.menu.vo.MenuConfigVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuConfigServiceImpl implements MenuConfigService {

    private static final String SERVICE_NAME = "menu-service";
    private static final String STOCK_ALERT_SCAN_INTERVAL = "stock_alert_scan_interval";

    private final MenuConfigMapper menuConfigMapper;
    private final StringRedisTemplate redisTemplate;

    public MenuConfigServiceImpl(MenuConfigMapper menuConfigMapper, StringRedisTemplate redisTemplate) {
        this.menuConfigMapper = menuConfigMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<MenuConfigVO> list() {
        List<MenuConfig> configs = menuConfigMapper.selectList(new LambdaQueryWrapper<MenuConfig>()
                .orderByAsc(MenuConfig::getConfigKey));
        configs.forEach(this::cache);
        return configs.stream().map(MenuConfigVO::from).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuConfigVO update(Long operatorId, UpdateMenuConfigRequest request) {
        String configKey = request.configKey().trim();
        MenuConfig config = menuConfigMapper.selectOne(new LambdaQueryWrapper<MenuConfig>()
                .eq(MenuConfig::getConfigKey, configKey));
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "菜单服务配置不存在");
        }
        String configValue = request.configValue().trim();
        validate(config, configValue);
        config.setConfigValue(configValue);
        config.setUpdatedBy(operatorId);
        menuConfigMapper.updateById(config);
        cache(config);
        return MenuConfigVO.from(config);
    }

    private void validate(MenuConfig config, String configValue) {
        if (STOCK_ALERT_SCAN_INTERVAL.equals(config.getConfigKey())) {
            try {
                if (Integer.parseInt(configValue) <= 0) {
                    throw invalidInterval();
                }
            } catch (NumberFormatException ex) {
                throw invalidInterval();
            }
        }
    }

    private BusinessException invalidInterval() {
        return new BusinessException(ErrorCode.BAD_REQUEST, "库存预警扫描间隔必须为正整数秒");
    }

    private void cache(MenuConfig config) {
        redisTemplate.opsForValue().set(cacheKey(config.getConfigKey()), config.getConfigValue());
    }

    private String cacheKey(String configKey) {
        return "config:" + SERVICE_NAME + ":" + configKey;
    }
}
