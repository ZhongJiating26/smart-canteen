package cn.zhongjiating.canteen.pickup.service.impl;

import cn.zhongjiating.canteen.pickup.dto.EnqueuePreparingRequest;
import cn.zhongjiating.canteen.pickup.entity.PreparingQueueItem;
import cn.zhongjiating.canteen.pickup.mapper.CallEventMapper;
import cn.zhongjiating.canteen.pickup.mapper.PickupQrcodeMapper;
import cn.zhongjiating.canteen.pickup.mapper.PreparingQueueItemMapper;
import cn.zhongjiating.canteen.pickup.mapper.WaitingQueueItemMapper;
import cn.zhongjiating.canteen.pickup.service.PickupNumberService;
import cn.zhongjiating.canteen.pickup.service.QueueCacheService;
import cn.zhongjiating.canteen.pickup.service.WebSocketPushService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PickupQueueServiceImplTest {

    @Mock
    private PickupNumberService pickupNumberService;
    @Mock
    private PreparingQueueItemMapper preparingQueueItemMapper;
    @Mock
    private WaitingQueueItemMapper waitingQueueItemMapper;
    @Mock
    private PickupQrcodeMapper pickupQrcodeMapper;
    @Mock
    private CallEventMapper callEventMapper;
    @Mock
    private QueueCacheService queueCacheService;
    @Mock
    private WebSocketPushService webSocketPushService;

    @Test
    void enqueuePreparingReturnsExistingPickupNumberWithoutDuplicatingQueueItem() {
        PreparingQueueItem item = new PreparingQueueItem();
        item.setOrderId(9001L);
        item.setPickupNo("A008");
        when(preparingQueueItemMapper.selectOne(any())).thenReturn(item);

        PickupQueueServiceImpl service = new PickupQueueServiceImpl(pickupNumberService, preparingQueueItemMapper,
                waitingQueueItemMapper, pickupQrcodeMapper, callEventMapper, queueCacheService,
                webSocketPushService, new ObjectMapper());

        assertThat(service.enqueuePreparing(new EnqueuePreparingRequest(2001L, 9001L)).pickupNo()).isEqualTo("A008");
        verify(preparingQueueItemMapper, never()).insert(any(PreparingQueueItem.class));
        verify(pickupNumberService, never()).generatePickupNo();
    }

    @Test
    void positionReportsPreparingQueueAheadCount() {
        PreparingQueueItem item = new PreparingQueueItem();
        item.setOrderId(9002L);
        item.setPickupNo("A009");
        item.setPosition(4);
        when(preparingQueueItemMapper.selectOne(any())).thenReturn(item);

        PickupQueueServiceImpl service = new PickupQueueServiceImpl(pickupNumberService, preparingQueueItemMapper,
                waitingQueueItemMapper, pickupQrcodeMapper, callEventMapper, queueCacheService,
                webSocketPushService, new ObjectMapper());

        assertThat(service.position(9002L).status()).isEqualTo("PREPARING");
        assertThat(service.position(9002L).preparingAhead()).isEqualTo(3);
    }
}
