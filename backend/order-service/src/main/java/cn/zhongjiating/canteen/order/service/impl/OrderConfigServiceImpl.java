package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.order.dto.UpdateOrderConfigRequest;
import cn.zhongjiating.canteen.order.entity.OrderConfig;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import cn.zhongjiating.canteen.order.service.OrderConfigService;
import cn.zhongjiating.canteen.order.vo.OrderConfigVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class OrderConfigServiceImpl implements OrderConfigService {

    private static final String SERVICE_NAME = "order-service";
    private static final Set<String> POSITIVE_MINUTE_KEYS = Set.of(
            "pay_timeout_minutes",
            "accept_timeout_minutes",
            "cancel_window_minutes"
    );

    private final OrderConfigMapper orderConfigMapper;
    private final StringRedisTemplate redisTemplate;

    public OrderConfigServiceImpl(OrderConfigMapper orderConfigMapper, StringRedisTemplate redisTemplate) {
        this.orderConfigMapper = orderConfigMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<OrderConfigVO> list() {
        List<OrderConfig> configs = orderConfigMapper.selectList(new LambdaQueryWrapper<OrderConfig>()
                .orderByAsc(OrderConfig::getConfigKey));
        configs.forEach(this::cache);
        return configs.stream().map(OrderConfigVO::from).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderConfigVO update(Long operatorId, UpdateOrderConfigRequest request) {
        String configKey = request.configKey().trim();
        OrderConfig config = orderConfigMapper.selectOne(new LambdaQueryWrapper<OrderConfig>()
                .eq(OrderConfig::getConfigKey, configKey));
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单服务配置不存在");
        }
        String configValue = request.configValue().trim();
        validate(config, configValue);
        config.setConfigValue(configValue);
        config.setUpdatedBy(operatorId);
        orderConfigMapper.updateById(config);
        cache(config);
        return OrderConfigVO.from(config);
    }

    private void validate(OrderConfig config, String configValue) {
        if (!POSITIVE_MINUTE_KEYS.contains(config.getConfigKey())) {
            return;
        }
        try {
            if (Integer.parseInt(configValue) <= 0) {
                throw invalidMinuteValue();
            }
        } catch (NumberFormatException ex) {
            throw invalidMinuteValue();
        }
    }

    private BusinessException invalidMinuteValue() {
        return new BusinessException(ErrorCode.BAD_REQUEST, "订单时间配置必须为正整数分钟");
    }

    private void cache(OrderConfig config) {
        redisTemplate.opsForValue().set(cacheKey(config.getConfigKey()), config.getConfigValue());
    }

    private String cacheKey(String configKey) {
        return "config:" + SERVICE_NAME + ":" + configKey;
    }
}
