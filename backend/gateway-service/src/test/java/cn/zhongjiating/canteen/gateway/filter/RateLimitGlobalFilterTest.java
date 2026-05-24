package cn.zhongjiating.canteen.gateway.filter;

import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.util.JwtUtil;
import cn.zhongjiating.canteen.gateway.config.GatewayJwtProperties;
import cn.zhongjiating.canteen.gateway.config.GatewayRateLimitProperties;
import cn.zhongjiating.canteen.gateway.ratelimit.TokenBucketRateLimiter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitGlobalFilterTest {

    private static final String SECRET = "smart-canteen-local-development-secret";

    @Test
    void requestPassesAllApplicableBuckets() {
        RecordingLimiter limiter = new RecordingLimiter(List.of(true, true, true));
        RateLimitGlobalFilter filter = filter(limiter);
        String token = JwtUtil.generateAccessToken(SECRET, 1000001L, "student", RoleEnum.USER, Duration.ofHours(1));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .post("/api/order/8000001/pay")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Forwarded-For", "10.0.0.8"));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, chain(forwarded)).block();

        assertThat(forwarded.get()).isNotNull();
        assertThat(limiter.calls).containsExactly(
                "ratelimit:ip:10_0_0_8|100|1",
                "ratelimit:user:1000001|50|1",
                "ratelimit:sensitive:10_0_0_8::api:order:8000001:pay|5|60"
        );
    }

    @Test
    void ipLimitRejectsBeforeOtherBuckets() {
        RecordingLimiter limiter = new RecordingLimiter(List.of(false));
        RateLimitGlobalFilter filter = filter(limiter);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .get("/api/menu/available")
                .header("X-Forwarded-For", "10.0.0.8"));

        filter.filter(exchange, chain(new AtomicReference<>())).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(limiter.calls).containsExactly("ratelimit:ip:10_0_0_8|100|1");
    }

    @Test
    void userLimitRejectsAuthenticatedRequest() {
        RecordingLimiter limiter = new RecordingLimiter(List.of(true, false));
        RateLimitGlobalFilter filter = filter(limiter);
        String token = JwtUtil.generateAccessToken(SECRET, 1000001L, "student", RoleEnum.USER, Duration.ofHours(1));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .get("/api/order/my")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Forwarded-For", "10.0.0.8"));

        filter.filter(exchange, chain(new AtomicReference<>())).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(limiter.calls).containsExactly(
                "ratelimit:ip:10_0_0_8|100|1",
                "ratelimit:user:1000001|50|1"
        );
    }

    @Test
    void sensitiveLimitAppliesWithoutToken() {
        RecordingLimiter limiter = new RecordingLimiter(List.of(true, false));
        RateLimitGlobalFilter filter = filter(limiter);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                .post("/api/user/send-code")
                .header("X-Forwarded-For", "10.0.0.8"));

        filter.filter(exchange, chain(new AtomicReference<>())).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(limiter.calls).containsExactly(
                "ratelimit:ip:10_0_0_8|100|1",
                "ratelimit:sensitive:10_0_0_8::api:user:send-code|5|60"
        );
    }

    private RateLimitGlobalFilter filter(TokenBucketRateLimiter limiter) {
        GatewayJwtProperties jwtProperties = new GatewayJwtProperties();
        jwtProperties.setSecret(SECRET);
        return new RateLimitGlobalFilter(new GatewayRateLimitProperties(), jwtProperties, limiter, new ObjectMapper());
    }

    private GatewayFilterChain chain(AtomicReference<ServerWebExchange> forwarded) {
        return exchange -> {
            forwarded.set(exchange);
            return Mono.empty();
        };
    }

    private static class RecordingLimiter implements TokenBucketRateLimiter {

        private final List<Boolean> results;
        private final List<String> calls = new ArrayList<>();
        private int index;

        private RecordingLimiter(List<Boolean> results) {
            this.results = results;
        }

        @Override
        public Mono<Boolean> allow(String key, int rate, int windowSeconds) {
            calls.add(key + "|" + rate + "|" + windowSeconds);
            boolean result = index < results.size() && results.get(index);
            index++;
            return Mono.just(result);
        }
    }
}
