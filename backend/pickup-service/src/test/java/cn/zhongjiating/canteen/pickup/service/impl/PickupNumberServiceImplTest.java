package cn.zhongjiating.canteen.pickup.service.impl;

import cn.zhongjiating.canteen.pickup.entity.PickupNumberSequence;
import cn.zhongjiating.canteen.pickup.mapper.PickupNumberSequenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PickupNumberServiceImplTest {

    @Mock
    private PickupNumberSequenceMapper sequenceMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void generateAdvancesGlobalSequenceAfterReturningCurrentNumber() {
        PickupNumberSequence sequence = new PickupNumberSequence();
        sequence.setId(1L);
        sequence.setCurrentLetter("A");
        sequence.setCurrentNumber(999);
        sequence.setResetRule("NONE");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("lock:pickup_seq"), any(), eq(3L), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(sequenceMapper.selectById(1L)).thenReturn(sequence);

        PickupNumberServiceImpl service = new PickupNumberServiceImpl(sequenceMapper, redisTemplate);

        assertThat(service.generatePickupNo()).isEqualTo("A999");
        assertThat(sequence.getCurrentLetter()).isEqualTo("B");
        assertThat(sequence.getCurrentNumber()).isEqualTo(1);
        verify(sequenceMapper).updateById(sequence);
    }
}
