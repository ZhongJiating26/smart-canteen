package cn.zhongjiating.canteen.pickup.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.ResetRule;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.pickup.dto.UpdatePickupConfigRequest;
import cn.zhongjiating.canteen.pickup.entity.PickupConfig;
import cn.zhongjiating.canteen.pickup.entity.PickupNumberSequence;
import cn.zhongjiating.canteen.pickup.mapper.PickupConfigMapper;
import cn.zhongjiating.canteen.pickup.mapper.PickupNumberSequenceMapper;
import cn.zhongjiating.canteen.pickup.service.PickupConfigService;
import cn.zhongjiating.canteen.pickup.vo.PickupConfigVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PickupConfigServiceImpl implements PickupConfigService {

    private static final String SERVICE_NAME = "pickup-service";
    private static final String PICKUP_NUMBER_RESET_RULE = "pickup_number_reset_rule";

    private final PickupConfigMapper pickupConfigMapper;
    private final PickupNumberSequenceMapper sequenceMapper;
    private final StringRedisTemplate redisTemplate;

    public PickupConfigServiceImpl(PickupConfigMapper pickupConfigMapper,
                                   PickupNumberSequenceMapper sequenceMapper,
                                   StringRedisTemplate redisTemplate) {
        this.pickupConfigMapper = pickupConfigMapper;
        this.sequenceMapper = sequenceMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<PickupConfigVO> list() {
        List<PickupConfig> configs = pickupConfigMapper.selectList(new LambdaQueryWrapper<PickupConfig>()
                .orderByAsc(PickupConfig::getConfigKey));
        configs.forEach(this::cache);
        return configs.stream().map(PickupConfigVO::from).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PickupConfigVO update(Long operatorId, UpdatePickupConfigRequest request) {
        String configKey = request.configKey().trim();
        PickupConfig config = pickupConfigMapper.selectOne(new LambdaQueryWrapper<PickupConfig>()
                .eq(PickupConfig::getConfigKey, configKey));
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "取餐服务配置不存在");
        }
        String configValue = request.configValue().trim().toUpperCase();
        validate(configKey, configValue);
        config.setConfigValue(configValue);
        config.setUpdatedBy(operatorId);
        pickupConfigMapper.updateById(config);
        syncSequenceResetRule(configKey, configValue);
        cache(config);
        return PickupConfigVO.from(config);
    }

    private void validate(String configKey, String configValue) {
        if (!PICKUP_NUMBER_RESET_RULE.equals(configKey)) {
            return;
        }
        try {
            ResetRule.valueOf(configValue);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "取餐号重置规则仅支持 NONE、DAILY 或 WEEKLY");
        }
    }

    private void syncSequenceResetRule(String configKey, String configValue) {
        if (!PICKUP_NUMBER_RESET_RULE.equals(configKey)) {
            return;
        }
        PickupNumberSequence sequence = sequenceMapper.selectById(1L);
        if (sequence != null) {
            sequence.setResetRule(configValue);
            sequenceMapper.updateById(sequence);
        }
    }

    private void cache(PickupConfig config) {
        redisTemplate.opsForValue().set("config:" + SERVICE_NAME + ":" + config.getConfigKey(), config.getConfigValue());
    }
}
