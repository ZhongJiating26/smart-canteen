package cn.zhongjiating.canteen.gateway.filter;

import cn.zhongjiating.canteen.common.dto.JwtClaims;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.UnauthorizedException;
import cn.zhongjiating.canteen.common.util.JwtUtil;
import cn.zhongjiating.canteen.gateway.config.GatewayJwtProperties;
import cn.zhongjiating.canteen.gateway.config.GatewayRateLimitProperties;
import cn.zhongjiating.canteen.gateway.ratelimit.TokenBucketRateLimiter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final GatewayRateLimitProperties properties;
    private final GatewayJwtProperties jwtProperties;
    private final TokenBucketRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public RateLimitGlobalFilter(GatewayRateLimitProperties properties, GatewayJwtProperties jwtProperties,
                                 TokenBucketRateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.properties = properties;
        this.jwtProperties = jwtProperties;
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String ip = clientIp(request);
        return rateLimiter.allow("ratelimit:ip:" + ip, properties.getIp(), 1)
                .flatMap(allowed -> allowed ? checkUserLimit(exchange, chain, ip) : writeRateLimited(exchange));
    }

    @Override
    public int getOrder() {
        return -200;
    }

    private Mono<Void> checkUserLimit(ServerWebExchange exchange, GatewayFilterChain chain, String ip) {
        JwtClaims claims = accessClaims(exchange.getRequest());
        if (claims == null) {
            return checkSensitiveLimit(exchange, chain, ip);
        }
        return rateLimiter.allow("ratelimit:user:" + claims.getUserId(), properties.getUser(), 1)
                .flatMap(allowed -> allowed ? checkSensitiveLimit(exchange, chain, ip) : writeRateLimited(exchange));
    }

    private Mono<Void> checkSensitiveLimit(ServerWebExchange exchange, GatewayFilterChain chain, String ip) {
        ServerHttpRequest request = exchange.getRequest();
        if (!isSensitive(request)) {
            return chain.filter(exchange);
        }
        String pathKey = sanitizePath(request.getPath().pathWithinApplication().value());
        return rateLimiter.allow("ratelimit:sensitive:" + ip + ":" + pathKey, properties.getSensitive(), 60)
                .flatMap(allowed -> allowed ? chain.filter(exchange) : writeRateLimited(exchange));
    }

    private JwtClaims accessClaims(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        try {
            JwtClaims claims = JwtUtil.parse(jwtProperties.getSecret(), authorization);
            if (JwtUtil.ACCESS_TOKEN.equals(claims.getTokenType()) && !claims.isExpired()) {
                return claims;
            }
        } catch (UnauthorizedException ignored) {
            return null;
        }
        return null;
    }

    private boolean isSensitive(ServerHttpRequest request) {
        String path = request.getPath().pathWithinApplication().value();
        HttpMethod method = request.getMethod();
        return properties.getSensitiveApis().stream()
                .anyMatch(api -> (api.method() == null || api.method() == method)
                        && PATH_MATCHER.match(api.pathPattern(), path));
    }

    private String clientIp(ServerHttpRequest request) {
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return sanitizeKeyPart(forwardedFor.split(",")[0].trim());
        }
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            return sanitizeKeyPart(request.getRemoteAddress().getAddress().getHostAddress());
        }
        return "unknown";
    }

    private String sanitizePath(String path) {
        return sanitizeKeyPart(path.replace('/', ':'));
    }

    private String sanitizeKeyPart(String value) {
        return value == null || value.isBlank() ? "unknown" : value.replaceAll("[^A-Za-z0-9:_-]", "_");
    }

    private Mono<Void> writeRateLimited(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = responseBytes();
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private byte[] responseBytes() {
        try {
            return objectMapper.writeValueAsBytes(Result.error(ErrorCode.RATE_LIMITED));
        } catch (JsonProcessingException ex) {
            return ("{\"code\":429,\"message\":\"" + ErrorCode.RATE_LIMITED.getMessage()
                    + "\",\"data\":null}").getBytes(StandardCharsets.UTF_8);
        }
    }
}
