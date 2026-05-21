package cn.zhongjiating.canteen.pickup.service.impl;

import cn.zhongjiating.canteen.common.enums.CallType;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.pickup.dto.CallPickupRequest;
import cn.zhongjiating.canteen.pickup.dto.EnqueuePreparingRequest;
import cn.zhongjiating.canteen.pickup.dto.QueueOrderRequest;
import cn.zhongjiating.canteen.pickup.entity.CallEvent;
import cn.zhongjiating.canteen.pickup.entity.PickupQrcode;
import cn.zhongjiating.canteen.pickup.entity.PreparingQueueItem;
import cn.zhongjiating.canteen.pickup.entity.WaitingQueueItem;
import cn.zhongjiating.canteen.pickup.mapper.CallEventMapper;
import cn.zhongjiating.canteen.pickup.mapper.PickupQrcodeMapper;
import cn.zhongjiating.canteen.pickup.mapper.PreparingQueueItemMapper;
import cn.zhongjiating.canteen.pickup.mapper.WaitingQueueItemMapper;
import cn.zhongjiating.canteen.pickup.service.PickupNumberService;
import cn.zhongjiating.canteen.pickup.service.PickupQueueService;
import cn.zhongjiating.canteen.pickup.service.QueueCacheService;
import cn.zhongjiating.canteen.pickup.service.WebSocketPushService;
import cn.zhongjiating.canteen.pickup.vo.CallEventVO;
import cn.zhongjiating.canteen.pickup.vo.PickupNoVO;
import cn.zhongjiating.canteen.pickup.vo.PickupPositionVO;
import cn.zhongjiating.canteen.pickup.vo.QrcodeVO;
import cn.zhongjiating.canteen.pickup.vo.QueueItemVO;
import cn.zhongjiating.canteen.pickup.vo.QueueVO;
import cn.zhongjiating.canteen.pickup.vo.SnapshotVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PickupQueueServiceImpl implements PickupQueueService {

    private final PickupNumberService pickupNumberService;
    private final PreparingQueueItemMapper preparingQueueItemMapper;
    private final WaitingQueueItemMapper waitingQueueItemMapper;
    private final PickupQrcodeMapper pickupQrcodeMapper;
    private final CallEventMapper callEventMapper;
    private final QueueCacheService queueCacheService;
    private final WebSocketPushService webSocketPushService;
    private final ObjectMapper objectMapper;

    public PickupQueueServiceImpl(PickupNumberService pickupNumberService,
                                  PreparingQueueItemMapper preparingQueueItemMapper,
                                  WaitingQueueItemMapper waitingQueueItemMapper,
                                  PickupQrcodeMapper pickupQrcodeMapper,
                                  CallEventMapper callEventMapper,
                                  QueueCacheService queueCacheService,
                                  WebSocketPushService webSocketPushService,
                                  ObjectMapper objectMapper) {
        this.pickupNumberService = pickupNumberService;
        this.preparingQueueItemMapper = preparingQueueItemMapper;
        this.waitingQueueItemMapper = waitingQueueItemMapper;
        this.pickupQrcodeMapper = pickupQrcodeMapper;
        this.callEventMapper = callEventMapper;
        this.queueCacheService = queueCacheService;
        this.webSocketPushService = webSocketPushService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PickupNoVO enqueuePreparing(EnqueuePreparingRequest request) {
        PreparingQueueItem preparing = findPreparing(request.orderId());
        if (preparing != null) {
            return new PickupNoVO(request.orderId(), preparing.getPickupNo());
        }
        WaitingQueueItem waiting = findWaiting(request.orderId());
        if (waiting != null) {
            return new PickupNoVO(request.orderId(), waiting.getPickupNo());
        }
        PreparingQueueItem item = new PreparingQueueItem();
        item.setMerchantId(request.merchantId());
        item.setOrderId(request.orderId());
        item.setPickupNo(pickupNumberService.generatePickupNo());
        item.setEnterTime(LocalDateTime.now());
        item.setPosition(preparingQueueItemMapper.selectMaxPosition(request.merchantId()) + 1);
        preparingQueueItemMapper.insert(item);
        queueCacheService.rebuildPreparing(request.merchantId());
        webSocketPushService.pushPreparingUpdate(request.merchantId());
        return new PickupNoVO(item.getOrderId(), item.getPickupNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QrcodeVO enqueueWaiting(QueueOrderRequest request) {
        PickupQrcode existingQrcode = findQrcode(request.orderId());
        WaitingQueueItem existingWaiting = findWaiting(request.orderId());
        if (existingQrcode != null && existingWaiting != null) {
            return QrcodeVO.from(existingQrcode, existingWaiting.getPickupNo());
        }
        PreparingQueueItem preparing = findPreparing(request.orderId());
        if (preparing == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS, "订单不在制作中队列");
        }
        PickupQrcode qrcode = existingQrcode == null ? createQrcode(request.orderId()) : existingQrcode;
        preparingQueueItemMapper.deleteById(preparing.getId());

        WaitingQueueItem waiting = new WaitingQueueItem();
        waiting.setMerchantId(request.merchantId());
        waiting.setOrderId(request.orderId());
        waiting.setPickupNo(preparing.getPickupNo());
        waiting.setEnterTime(LocalDateTime.now());
        waiting.setPosition(waitingQueueItemMapper.selectMaxPosition(request.merchantId()) + 1);
        waitingQueueItemMapper.insert(waiting);
        recordCall(waiting, CallType.AUTO);
        queueCacheService.rebuildPreparing(request.merchantId());
        queueCacheService.rebuildWaiting(request.merchantId());
        webSocketPushService.pushPreparingUpdate(request.merchantId());
        webSocketPushService.pushWaitingUpdate(request.merchantId());
        webSocketPushService.pushCallNotify(request.merchantId(), request.orderId(), waiting.getPickupNo(), CallType.AUTO);
        return QrcodeVO.from(qrcode, waiting.getPickupNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeQueue(QueueOrderRequest request) {
        PreparingQueueItem preparing = findPreparing(request.orderId());
        WaitingQueueItem waiting = findWaiting(request.orderId());
        if (preparing != null) {
            preparingQueueItemMapper.deleteById(preparing.getId());
            queueCacheService.rebuildPreparing(preparing.getMerchantId());
            webSocketPushService.pushPreparingUpdate(preparing.getMerchantId());
        }
        if (waiting != null) {
            waitingQueueItemMapper.deleteById(waiting.getId());
            queueCacheService.rebuildWaiting(waiting.getMerchantId());
            webSocketPushService.pushWaitingUpdate(waiting.getMerchantId());
        }
    }

    @Override
    public QueueVO preparing(Long merchantId) {
        List<QueueItemVO> queue = queueCacheService.preparing(merchantId);
        return new QueueVO(merchantId, queue, queue.size());
    }

    @Override
    public QueueVO waiting(Long merchantId) {
        List<QueueItemVO> queue = queueCacheService.waiting(merchantId);
        return new QueueVO(merchantId, queue, queue.size());
    }

    @Override
    public SnapshotVO snapshot(Long merchantId) {
        List<CallEventVO> recentCalls = callEventMapper.selectList(new LambdaQueryWrapper<CallEvent>()
                        .eq(CallEvent::getMerchantId, merchantId)
                        .orderByDesc(CallEvent::getCallTime)
                        .last("LIMIT 30"))
                .stream()
                .map(CallEventVO::from)
                .toList();
        return new SnapshotVO(merchantId, queueCacheService.rebuildPreparing(merchantId),
                queueCacheService.rebuildWaiting(merchantId), recentCalls);
    }

    @Override
    public PickupPositionVO position(Long orderId) {
        PreparingQueueItem preparing = findPreparing(orderId);
        if (preparing != null) {
            int ahead = Math.max(0, preparing.getPosition() - 1);
            return new PickupPositionVO(orderId, preparing.getPickupNo(), "PREPARING",
                    preparing.getPosition(), ahead, null, null);
        }
        WaitingQueueItem waiting = findWaiting(orderId);
        if (waiting != null) {
            int ahead = Math.max(0, waiting.getPosition() - 1);
            return new PickupPositionVO(orderId, waiting.getPickupNo(), "READY",
                    null, null, waiting.getPosition(), ahead);
        }
        throw new BusinessException(ErrorCode.NOT_FOUND, "订单不在取餐队列中");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void call(CallPickupRequest request) {
        WaitingQueueItem waiting = findWaiting(request.orderId());
        if (waiting == null || !waiting.getMerchantId().equals(request.merchantId())) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS, "订单不在当前窗口待取餐队列");
        }
        recordCall(waiting, CallType.MANUAL);
        webSocketPushService.pushCallNotify(request.merchantId(), request.orderId(), waiting.getPickupNo(), CallType.MANUAL);
    }

    @Override
    public QrcodeVO qrcode(Long orderId) {
        PickupQrcode qrcode = findQrcode(orderId);
        WaitingQueueItem waiting = findWaiting(orderId);
        if (qrcode == null || waiting == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "取餐二维码不存在");
        }
        return QrcodeVO.from(qrcode, waiting.getPickupNo());
    }

    private PickupQrcode createQrcode(Long orderId) {
        PickupQrcode qrcode = new PickupQrcode();
        qrcode.setOrderId(orderId);
        qrcode.setPickupCode(UUID.randomUUID().toString().replace("-", ""));
        qrcode.setQrcodeContent(qrcodeContent(orderId, qrcode.getPickupCode()));
        qrcode.setIsVerified(0);
        qrcode.setCreateTime(LocalDateTime.now());
        pickupQrcodeMapper.insert(qrcode);
        return qrcode;
    }

    private String qrcodeContent(Long orderId, String pickupCode) {
        try {
            return objectMapper.writeValueAsString(Map.of("orderId", orderId, "code", pickupCode));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("取餐二维码内容无法序列化", ex);
        }
    }

    private void recordCall(WaitingQueueItem waiting, CallType callType) {
        CallEvent event = new CallEvent();
        event.setMerchantId(waiting.getMerchantId());
        event.setOrderId(waiting.getOrderId());
        event.setPickupNo(waiting.getPickupNo());
        event.setCallType(callType.name());
        event.setCallTime(LocalDateTime.now());
        callEventMapper.insert(event);
    }

    private PreparingQueueItem findPreparing(Long orderId) {
        return preparingQueueItemMapper.selectOne(new LambdaQueryWrapper<PreparingQueueItem>()
                .eq(PreparingQueueItem::getOrderId, orderId));
    }

    private WaitingQueueItem findWaiting(Long orderId) {
        return waitingQueueItemMapper.selectOne(new LambdaQueryWrapper<WaitingQueueItem>()
                .eq(WaitingQueueItem::getOrderId, orderId));
    }

    private PickupQrcode findQrcode(Long orderId) {
        return pickupQrcodeMapper.selectOne(new LambdaQueryWrapper<PickupQrcode>()
                .eq(PickupQrcode::getOrderId, orderId));
    }
}
