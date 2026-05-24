package cn.zhongjiating.canteen.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "gateway.rate-limit")
public class GatewayRateLimitProperties {

    private int ip = 100;
    private int user = 50;
    private int sensitive = 5;
    private List<SensitiveApi> sensitiveApis = new ArrayList<>(List.of(
            new SensitiveApi(HttpMethod.POST, "/api/user/send-code"),
            new SensitiveApi(HttpMethod.POST, "/api/user/register"),
            new SensitiveApi(HttpMethod.POST, "/api/user/login/**"),
            new SensitiveApi(HttpMethod.POST, "/api/user/refresh"),
            new SensitiveApi(HttpMethod.POST, "/api/order/*/pay"),
            new SensitiveApi(HttpMethod.POST, "/api/pickup/verify")
    ));

    public int getIp() {
        return ip;
    }

    public void setIp(int ip) {
        this.ip = ip;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getSensitive() {
        return sensitive;
    }

    public void setSensitive(int sensitive) {
        this.sensitive = sensitive;
    }

    public List<SensitiveApi> getSensitiveApis() {
        return sensitiveApis;
    }

    public void setSensitiveApis(List<SensitiveApi> sensitiveApis) {
        this.sensitiveApis = sensitiveApis == null ? List.of() : sensitiveApis;
    }

    public record SensitiveApi(HttpMethod method, String pathPattern) {
    }
}
