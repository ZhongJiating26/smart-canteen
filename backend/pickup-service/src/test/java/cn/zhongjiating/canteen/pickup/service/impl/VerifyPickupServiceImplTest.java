package cn.zhongjiating.canteen.pickup.service.impl;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.pickup.dto.VerifyPickupRequest;
import cn.zhongjiating.canteen.pickup.entity.PickupQrcode;
import cn.zhongjiating.canteen.pickup.entity.VerifyRecord;
import cn.zhongjiating.canteen.pickup.entity.WaitingQueueItem;
import cn.zhongjiating.canteen.pickup.feign.OrderFeignClient;
import cn.zhongjiating.canteen.pickup.mapper.PickupQrcodeMapper;
import cn.zhongjiating.canteen.pickup.mapper.VerifyRecordMapper;
import cn.zhongjiating.canteen.pickup.mapper.WaitingQueueItemMapper;
import cn.zhongjiating.canteen.pickup.service.QueueCacheService;
import cn.zhongjiating.canteen.pickup.service.WebSocketPushService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyPickupServiceImplTest {

    @Mock
    private PickupQrcodeMapper pickupQrcodeMapper;
    @Mock
    private WaitingQueueItemMapper waitingQueueItemMapper;
    @Mock
    private VerifyRecordMapper verifyRecordMapper;
    @Mock
    private QueueCacheService queueCacheService;
    @Mock
    private WebSocketPushService webSocketPushService;
    @Mock
    private OrderFeignClient orderFeignClient;

    @Test
    void manualVerifyCompletesOrderAndRemovesWaitingQueueItem() {
        PickupQrcode qrcode = new PickupQrcode();
        qrcode.setId(61L);
        qrcode.setOrderId(9001L);
        qrcode.setPickupCode("manual-code");
        qrcode.setIsVerified(0);
        WaitingQueueItem waiting = new WaitingQueueItem();
        waiting.setId(71L);
        waiting.setMerchantId(2001L);
        waiting.setOrderId(9001L);
        when(pickupQrcodeMapper.selectOne(any())).thenReturn(qrcode);
        when(waitingQueueItemMapper.selectOne(any())).thenReturn(waiting);
        when(orderFeignClient.complete(9001L)).thenReturn(Result.success());

        VerifyPickupServiceImpl service = new VerifyPickupServiceImpl(pickupQrcodeMapper, waitingQueueItemMapper,
                verifyRecordMapper, queueCacheService, webSocketPushService, orderFeignClient, new ObjectMapper());

        assertThat(service.verify(2001L, new VerifyPickupRequest(2001L, null, "manual-code", "MANUAL")).orderId())
                .isEqualTo(9001L);
        verify(orderFeignClient).complete(9001L);
        verify(pickupQrcodeMapper).updateById(qrcode);
        verify(waitingQueueItemMapper).deleteById(71L);
        verify(verifyRecordMapper).insert(any(VerifyRecord.class));
        verify(queueCacheService).rebuildWaiting(2001L);
    }
}
