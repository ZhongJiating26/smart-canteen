package cn.zhongjiating.canteen.pickup.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.VerifyType;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.pickup.dto.VerifyPickupRequest;
import cn.zhongjiating.canteen.pickup.entity.PickupQrcode;
import cn.zhongjiating.canteen.pickup.entity.VerifyRecord;
import cn.zhongjiating.canteen.pickup.entity.WaitingQueueItem;
import cn.zhongjiating.canteen.pickup.feign.OrderFeignClient;
import cn.zhongjiating.canteen.pickup.mapper.PickupQrcodeMapper;
import cn.zhongjiating.canteen.pickup.mapper.VerifyRecordMapper;
import cn.zhongjiating.canteen.pickup.mapper.WaitingQueueItemMapper;
import cn.zhongjiating.canteen.pickup.service.QueueCacheService;
import cn.zhongjiating.canteen.pickup.service.VerifyPickupService;
import cn.zhongjiating.canteen.pickup.service.WebSocketPushService;
import cn.zhongjiating.canteen.pickup.vo.VerifyPickupVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VerifyPickupServiceImpl implements VerifyPickupService {

    private final PickupQrcodeMapper pickupQrcodeMapper;
    private final WaitingQueueItemMapper waitingQueueItemMapper;
    private final VerifyRecordMapper verifyRecordMapper;
    private final QueueCacheService queueCacheService;
    private final WebSocketPushService webSocketPushService;
    private final OrderFeignClient orderFeignClient;
    private final ObjectMapper objectMapper;

    public VerifyPickupServiceImpl(PickupQrcodeMapper pickupQrcodeMapper,
                                   WaitingQueueItemMapper waitingQueueItemMapper,
                                   VerifyRecordMapper verifyRecordMapper,
                                   QueueCacheService queueCacheService,
                                   WebSocketPushService webSocketPushService,
                                   OrderFeignClient orderFeignClient,
                                   ObjectMapper objectMapper) {
        this.pickupQrcodeMapper = pickupQrcodeMapper;
        this.waitingQueueItemMapper = waitingQueueItemMapper;
        this.verifyRecordMapper = verifyRecordMapper;
        this.queueCacheService = queueCacheService;
        this.webSocketPushService = webSocketPushService;
        this.orderFeignClient = orderFeignClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerifyPickupVO verify(Long operatorId, VerifyPickupRequest request) {
        VerifyType verifyType = verifyType(request.verifyType());
        String pickupCode = pickupCode(request, verifyType);
        PickupQrcode qrcode = pickupQrcodeMapper.selectOne(new LambdaQueryWrapper<PickupQrcode>()
                .eq(PickupQrcode::getPickupCode, pickupCode));
        if (qrcode == null || qrcode.getIsVerified() == 1) {
            throw new BusinessException(ErrorCode.INVALID_PICKUP_CODE);
        }
        WaitingQueueItem waiting = waitingQueueItemMapper.selectOne(new LambdaQueryWrapper<WaitingQueueItem>()
                .eq(WaitingQueueItem::getOrderId, qrcode.getOrderId()));
        if (waiting == null) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS);
        }
        if (!waiting.getMerchantId().equals(request.merchantId())) {
            throw new BusinessException(ErrorCode.WRONG_STALL);
        }
        completeOrder(qrcode.getOrderId());
        LocalDateTime verifyTime = LocalDateTime.now();
        qrcode.setIsVerified(1);
        qrcode.setVerifyTime(verifyTime);
        pickupQrcodeMapper.updateById(qrcode);
        waitingQueueItemMapper.deleteById(waiting.getId());
        insertRecord(operatorId, verifyType, qrcode, waiting, verifyTime);
        queueCacheService.rebuildWaiting(waiting.getMerchantId());
        webSocketPushService.pushWaitingUpdate(waiting.getMerchantId());
        return new VerifyPickupVO(qrcode.getOrderId(), qrcode.getId(), verifyTime);
    }

    private VerifyType verifyType(String code) {
        try {
            return VerifyType.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "核销方式仅支持 SCAN 或 MANUAL");
        }
    }

    private String pickupCode(VerifyPickupRequest request, VerifyType verifyType) {
        if (verifyType == VerifyType.MANUAL) {
            if (request.pickupCode() == null || request.pickupCode().isBlank()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "手动核销需要取餐码");
            }
            return request.pickupCode().trim();
        }
        if (request.qrcodeContent() == null || request.qrcodeContent().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "扫码核销需要二维码内容");
        }
        try {
            JsonNode root = objectMapper.readTree(request.qrcodeContent());
            JsonNode code = root.get("code");
            if (code == null || code.asText().isBlank()) {
                throw invalidQrcode();
            }
            return code.asText();
        } catch (Exception ex) {
            throw invalidQrcode();
        }
    }

    private BusinessException invalidQrcode() {
        return new BusinessException(ErrorCode.INVALID_PICKUP_CODE, "二维码内容无效");
    }

    private void completeOrder(Long orderId) {
        Result<Void> result = orderFeignClient.complete(orderId);
        if (result == null || result.getCode() == null || result.getCode() != 200) {
            String message = result == null || result.getMessage() == null || result.getMessage().isBlank()
                    ? "订单核销同步失败" : result.getMessage();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, message);
        }
    }

    private void insertRecord(Long operatorId, VerifyType verifyType, PickupQrcode qrcode,
                              WaitingQueueItem waiting, LocalDateTime verifyTime) {
        VerifyRecord record = new VerifyRecord();
        record.setOrderId(qrcode.getOrderId());
        record.setQrcodeId(qrcode.getId());
        record.setMerchantId(waiting.getMerchantId());
        record.setVerifyType(verifyType.name());
        record.setOperatorId(operatorId);
        record.setVerifyTime(verifyTime);
        verifyRecordMapper.insert(record);
    }
}
