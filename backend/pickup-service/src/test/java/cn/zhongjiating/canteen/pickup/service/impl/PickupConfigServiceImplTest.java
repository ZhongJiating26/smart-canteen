package cn.zhongjiating.canteen.pickup.service.impl;

import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.pickup.dto.UpdatePickupConfigRequest;
import cn.zhongjiating.canteen.pickup.entity.PickupConfig;
import cn.zhongjiating.canteen.pickup.entity.PickupNumberSequence;
import cn.zhongjiating.canteen.pickup.mapper.PickupConfigMapper;
import cn.zhongjiating.canteen.pickup.mapper.PickupNumberSequenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PickupConfigServiceImplTest {

    @Mock
    private PickupConfigMapper pickupConfigMapper;
    @Mock
    private PickupNumberSequenceMapper sequenceMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void updateSyncsResetRuleToSequenceAndRedis() {
        PickupConfig config = resetRuleConfig();
        PickupNumberSequence sequence = new PickupNumberSequence();
        sequence.setId(1L);
        sequence.setResetRule("NONE");
        when(pickupConfigMapper.selectOne(any())).thenReturn(config);
        when(sequenceMapper.selectById(1L)).thenReturn(sequence);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        PickupConfigServiceImpl service = new PickupConfigServiceImpl(pickupConfigMapper, sequenceMapper, redisTemplate);
        service.update(1003L, new UpdatePickupConfigRequest("pickup_number_reset_rule", "daily"));

        verify(pickupConfigMapper).updateById(config);
        verify(sequenceMapper).updateById(sequence);
        verify(valueOperations).set("config:pickup-service:pickup_number_reset_rule", "DAILY");
    }

    @Test
    void updateRejectsUnknownResetRule() {
        when(pickupConfigMapper.selectOne(any())).thenReturn(resetRuleConfig());
        PickupConfigServiceImpl service = new PickupConfigServiceImpl(pickupConfigMapper, sequenceMapper, redisTemplate);

        assertThatThrownBy(() -> service.update(1003L,
                new UpdatePickupConfigRequest("pickup_number_reset_rule", "MONTHLY")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("取餐号重置规则仅支持 NONE、DAILY 或 WEEKLY");
    }

    private PickupConfig resetRuleConfig() {
        PickupConfig config = new PickupConfig();
        config.setId(1L);
        config.setConfigKey("pickup_number_reset_rule");
        config.setConfigValue("NONE");
        config.setConfigType("STRING");
        return config;
    }
}
