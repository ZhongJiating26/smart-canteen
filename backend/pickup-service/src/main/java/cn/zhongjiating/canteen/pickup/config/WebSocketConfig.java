package cn.zhongjiating.canteen.pickup.config;

import cn.zhongjiating.canteen.pickup.handler.ScreenWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ScreenWebSocketHandler screenWebSocketHandler;

    public WebSocketConfig(ScreenWebSocketHandler screenWebSocketHandler) {
        this.screenWebSocketHandler = screenWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(screenWebSocketHandler, "/ws/screen/{merchantId}").setAllowedOrigins("*");
    }
}
