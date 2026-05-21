package cn.zhongjiating.canteen.pickup.handler;

import cn.zhongjiating.canteen.pickup.service.WebSocketPushService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ScreenWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketPushService webSocketPushService;

    public ScreenWebSocketHandler(WebSocketPushService webSocketPushService) {
        this.webSocketPushService = webSocketPushService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        webSocketPushService.register(merchantId(session), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        webSocketPushService.unregister(merchantId(session), session);
    }

    private Long merchantId(WebSocketSession session) {
        String path = session.getUri() == null ? "" : session.getUri().getPath();
        String value = path.substring(path.lastIndexOf('/') + 1);
        return Long.valueOf(value);
    }
}
