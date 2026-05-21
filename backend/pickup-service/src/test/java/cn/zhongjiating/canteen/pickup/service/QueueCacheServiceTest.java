package cn.zhongjiating.canteen.pickup.service;

import cn.zhongjiating.canteen.pickup.entity.PreparingQueueItem;
import cn.zhongjiating.canteen.pickup.mapper.PreparingQueueItemMapper;
import cn.zhongjiating.canteen.pickup.mapper.WaitingQueueItemMapper;
import cn.zhongjiating.canteen.pickup.vo.QueueItemVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueCacheServiceTest {

    @Mock
    private PreparingQueueItemMapper preparingQueueItemMapper;
    @Mock
    private WaitingQueueItemMapper waitingQueueItemMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ListOperations<String, String> listOperations;

    @Test
    void preparingRebuildsRedisSnapshotWhenCacheMisses() {
        PreparingQueueItem item = new PreparingQueueItem();
        item.setPickupNo("A012");
        item.setPosition(2);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("queue:preparing:2001", 0, -1)).thenReturn(List.of());
        when(preparingQueueItemMapper.selectList(any())).thenReturn(List.of(item));

        QueueCacheService service = new QueueCacheService(preparingQueueItemMapper, waitingQueueItemMapper,
                redisTemplate, new ObjectMapper());

        List<QueueItemVO> queue = service.preparing(2001L);

        assertThat(queue).hasSize(1);
        assertThat(queue.get(0).pickupNo()).isEqualTo("A012");
        verify(redisTemplate).delete("queue:preparing:2001");
        verify(listOperations).rightPushAll(eq("queue:preparing:2001"), any(List.class));
    }
}
