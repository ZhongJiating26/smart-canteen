package cn.zhongjiating.canteen.gateway.filter;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.util.JwtUtil;
import cn.zhongjiating.canteen.gateway.config.GatewayJwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthGlobalFilterTest {

    private static final String SECRET = "smart-canteen-local-development-secret";

    @Test
    void protectedRequestInjectsUserHeaders() {
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        when(redisTemplate.hasKey(org.mockito.ArgumentMatchers.anyString())).thenReturn(Mono.just(false));
        AuthGlobalFilter filter = filter(redisTemplate);
        String token = JwtUtil.generateAccessToken(SECRET, 1000001L, "student", RoleEnum.USER, Duration.ofHours(1));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .post("/api/order/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(UserContext.HEADER_USER_ID, "spoofed"));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, chain(forwarded)).block();

        assertThat(forwarded.get().getRequest().getHeaders().getFirst(UserContext.HEADER_USER_ID))
                .isEqualTo("1000001");
        assertThat(forwarded.get().getRequest().getHeaders().getFirst(UserContext.HEADER_USER_ROLE))
                .isEqualTo("USER");
        assertThat(forwarded.get().getRequest().getHeaders().getFirst(UserContext.HEADER_USERNAME))
                .isEqualTo("student");
    }

    @Test
    void publicRequestStripsSpoofedUserHeaders() {
        AuthGlobalFilter filter = filter(mock(ReactiveStringRedisTemplate.class));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .get("/api/menu/available")
                .header(UserContext.HEADER_USER_ID, "spoofed"));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, chain(forwarded)).block();

        assertThat(forwarded.get().getRequest().getHeaders().containsKey(UserContext.HEADER_USER_ID)).isFalse();
    }

    @Test
    void merchantEndpointRejectsUserRole() {
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        when(redisTemplate.hasKey(org.mockito.ArgumentMatchers.anyString())).thenReturn(Mono.just(false));
        AuthGlobalFilter filter = filter(redisTemplate);
        String token = JwtUtil.generateAccessToken(SECRET, 1000001L, "student", RoleEnum.USER, Duration.ofHours(1));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .post("/api/order/merchant/8000001/accept")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));

        filter.filter(exchange, chain(new AtomicReference<>())).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void merchantDishListDoesNotUsePublicDishDetailRule() {
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        when(redisTemplate.hasKey(org.mockito.ArgumentMatchers.anyString())).thenReturn(Mono.just(false));
        AuthGlobalFilter filter = filter(redisTemplate);
        String token = JwtUtil.generateAccessToken(SECRET, 1000001L, "student", RoleEnum.USER, Duration.ofHours(1));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .get("/api/menu/dishes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));

        filter.filter(exchange, chain(new AtomicReference<>())).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void currentMerchantEndpointRejectsUserRole() {
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        when(redisTemplate.hasKey(org.mockito.ArgumentMatchers.anyString())).thenReturn(Mono.just(false));
        AuthGlobalFilter filter = filter(redisTemplate);
        String token = JwtUtil.generateAccessToken(SECRET, 1000001L, "student", RoleEnum.USER, Duration.ofHours(1));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .get("/api/menu/merchant/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));

        filter.filter(exchange, chain(new AtomicReference<>())).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void blacklistedTokenIsUnauthorized() {
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
        when(redisTemplate.hasKey(org.mockito.ArgumentMatchers.anyString())).thenReturn(Mono.just(true));
        AuthGlobalFilter filter = filter(redisTemplate);
        String token = JwtUtil.generateAccessToken(SECRET, 1000001L, "student", RoleEnum.USER, Duration.ofHours(1));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .get("/api/order/my")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));

        filter.filter(exchange, chain(new AtomicReference<>())).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private AuthGlobalFilter filter(ReactiveStringRedisTemplate redisTemplate) {
        GatewayJwtProperties properties = new GatewayJwtProperties();
        properties.setSecret(SECRET);
        return new AuthGlobalFilter(properties, redisTemplate, new ObjectMapper());
    }

    private GatewayFilterChain chain(AtomicReference<ServerWebExchange> forwarded) {
        return exchange -> {
            forwarded.set(exchange);
            return Mono.empty();
        };
    }
}
