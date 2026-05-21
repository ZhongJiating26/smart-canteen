package cn.zhongjiating.canteen.pickup.service;

import cn.zhongjiating.canteen.common.enums.CallType;
import cn.zhongjiating.canteen.pickup.vo.QueueItemVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketPushService {

    private final QueueCacheService queueCacheService;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public WebSocketPushService(QueueCacheService queueCacheService, ObjectMapper objectMapper) {
        this.queueCacheService = queueCacheService;
        this.objectMapper = objectMapper;
    }

    public void register(Long merchantId, WebSocketSession session) {
        sessions.computeIfAbsent(merchantId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(Long merchantId, WebSocketSession session) {
        Set<WebSocketSession> merchantSessions = sessions.get(merchantId);
        if (merchantSessions == null) {
            return;
        }
        merchantSessions.remove(session);
        if (merchantSessions.isEmpty()) {
            sessions.remove(merchantId);
        }
    }

    public void pushPreparingUpdate(Long merchantId) {
        send(merchantId, "PREPARING_UPDATE",
                Map.of("preparingList", queueCacheService.preparing(merchantId)));
    }

    public void pushWaitingUpdate(Long merchantId) {
        send(merchantId, "WAITING_UPDATE",
                Map.of("waitingList", queueCacheService.waiting(merchantId)));
    }

    public void pushCallNotify(Long merchantId, Long orderId, String pickupNo, CallType callType) {
        send(merchantId, "CALL_NOTIFY",
                Map.of("orderId", orderId, "pickupNo", pickupNo, "callType", callType.name()));
    }

    private void send(Long merchantId, String type, Object data) {
        Set<WebSocketSession> merchantSessions = sessions.get(merchantId);
        if (merchantSessions == null || merchantSessions.isEmpty()) {
            return;
        }
        String payload = payload(type, merchantId, data);
        for (WebSocketSession session : merchantSessions) {
            if (!session.isOpen()) {
                unregister(merchantId, session);
                continue;
            }
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException ex) {
                unregister(merchantId, session);
            }
        }
    }

    private String payload(String type, Long merchantId, Object data) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "type", type,
                    "merchantId", merchantId,
                    "data", data,
                    "timestamp", LocalDateTime.now()
            ));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("WebSocket 消息无法序列化", ex);
        }
    }
}
