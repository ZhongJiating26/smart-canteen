package cn.zhongjiating.canteen.gateway.filter;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.dto.JwtClaims;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.exception.UnauthorizedException;
import cn.zhongjiating.canteen.common.util.JwtUtil;
import cn.zhongjiating.canteen.gateway.config.GatewayJwtProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
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
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<Endpoint> PUBLIC_ENDPOINTS = List.of(
            Endpoint.any("/actuator/**"),
            Endpoint.any("/ws/**"),
            Endpoint.post("/api/user/send-code"),
            Endpoint.post("/api/user/register"),
            Endpoint.post("/api/user/login/password"),
            Endpoint.post("/api/user/login/code"),
            Endpoint.post("/api/user/refresh"),
            Endpoint.get("/api/menu/available"),
            Endpoint.get("/api/menu/categories"),
            Endpoint.get("/api/menu/dishes/**"),
            Endpoint.get("/api/pickup/preparing/**"),
            Endpoint.get("/api/pickup/waiting/**"),
            Endpoint.get("/api/pickup/snapshot/**")
    );
    private static final List<RoleRule> ROLE_RULES = List.of(
            new RoleRule("/api/admin/**", RoleEnum.ADMIN),
            new RoleRule("/api/order/merchant/**", RoleEnum.MERCHANT),
            new RoleRule("/api/menu/daily-menus/**", RoleEnum.MERCHANT),
            new RoleRule("/api/menu/inventory/**", RoleEnum.MERCHANT),
            new RoleRule("/api/menu/categories/**", RoleEnum.MERCHANT),
            new RoleRule("/api/menu/dishes/**", RoleEnum.MERCHANT),
            new RoleRule("/api/pickup/call", RoleEnum.MERCHANT),
            new RoleRule("/api/pickup/verify", RoleEnum.MERCHANT)
    );

    private final GatewayJwtProperties jwtProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public AuthGlobalFilter(GatewayJwtProperties jwtProperties, ReactiveStringRedisTemplate redisTemplate,
                            ObjectMapper objectMapper) {
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (isPublic(request)) {
            return chain.filter(stripUserHeaders(exchange));
        }
        JwtClaims claims;
        try {
            claims = JwtUtil.parse(jwtProperties.getSecret(), request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
            if (!JwtUtil.ACCESS_TOKEN.equals(claims.getTokenType()) || claims.isExpired()) {
                return writeError(exchange, ErrorCode.UNAUTHORIZED);
            }
        } catch (UnauthorizedException ex) {
            return writeError(exchange, ErrorCode.UNAUTHORIZED);
        }
        RoleEnum requiredRole = requiredRole(request.getPath().pathWithinApplication().value());
        if (requiredRole != null && requiredRole != claims.getRole()) {
            return writeError(exchange, ErrorCode.FORBIDDEN);
        }
        return redisTemplate.hasKey("token:blacklist:" + claims.getJti())
                .flatMap(blacklisted -> Boolean.TRUE.equals(blacklisted)
                        ? writeError(exchange, ErrorCode.UNAUTHORIZED)
                        : chain.filter(withUserHeaders(exchange, claims)));
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublic(ServerHttpRequest request) {
        String path = request.getPath().pathWithinApplication().value();
        HttpMethod method = request.getMethod();
        return PUBLIC_ENDPOINTS.stream().anyMatch(endpoint -> endpoint.matches(method, path));
    }

    private RoleEnum requiredRole(String path) {
        return ROLE_RULES.stream()
                .filter(rule -> PATH_MATCHER.match(rule.pathPattern(), path))
                .map(RoleRule::role)
                .findFirst()
                .orElse(null);
    }

    private ServerWebExchange stripUserHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(UserContext.HEADER_USER_ID);
                    headers.remove(UserContext.HEADER_USER_ROLE);
                    headers.remove(UserContext.HEADER_USERNAME);
                })
                .build();
        return exchange.mutate().request(request).build();
    }

    private ServerWebExchange withUserHeaders(ServerWebExchange exchange, JwtClaims claims) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(UserContext.HEADER_USER_ID);
                    headers.remove(UserContext.HEADER_USER_ROLE);
                    headers.remove(UserContext.HEADER_USERNAME);
                })
                .header(UserContext.HEADER_USER_ID, String.valueOf(claims.getUserId()))
                .header(UserContext.HEADER_USER_ROLE, claims.getRole().getCode())
                .header(UserContext.HEADER_USERNAME, claims.getUsername())
                .build();
        return exchange.mutate().request(request).build();
    }

    private Mono<Void> writeError(ServerWebExchange exchange, ErrorCode errorCode) {
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(errorCode.getHttpStatus()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = responseBytes(errorCode);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private byte[] responseBytes(ErrorCode errorCode) {
        try {
            return objectMapper.writeValueAsBytes(Result.error(errorCode));
        } catch (JsonProcessingException ex) {
            return ("{\"code\":" + errorCode.getHttpStatus() + ",\"message\":\""
                    + errorCode.getMessage() + "\",\"data\":null}").getBytes(StandardCharsets.UTF_8);
        }
    }

    private record Endpoint(HttpMethod method, String pathPattern) {

        private static Endpoint any(String pathPattern) {
            return new Endpoint(null, pathPattern);
        }

        private static Endpoint get(String pathPattern) {
            return new Endpoint(HttpMethod.GET, pathPattern);
        }

        private static Endpoint post(String pathPattern) {
            return new Endpoint(HttpMethod.POST, pathPattern);
        }

        private boolean matches(HttpMethod requestMethod, String path) {
            return (method == null || method == requestMethod) && PATH_MATCHER.match(pathPattern, path);
        }
    }

    private record RoleRule(String pathPattern, RoleEnum role) {
    }
}
