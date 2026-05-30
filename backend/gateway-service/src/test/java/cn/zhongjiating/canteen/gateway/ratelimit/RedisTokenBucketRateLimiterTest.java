package cn.zhongjiating.canteen.gateway.ratelimit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import reactor.core.publisher.Flux;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisTokenBucketRateLimiterTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Test
    void allow_RateOrWindowLessOrEqualZero_ReturnsFalse() {
        RedisTokenBucketRateLimiter limiter = new RedisTokenBucketRateLimiter(redisTemplate);

        Boolean result1 = limiter.allow("key1", 0, 10).block();
        assertFalse(result1 != null && result1);

        Boolean result2 = limiter.allow("key1", 10, -5).block();
        assertFalse(result2 != null && result2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void allow_RedisReturnsOne_ReturnsTrue() {
        Clock fixedClock = Clock.fixed(Instant.ofEpochMilli(1000000L), ZoneId.of("UTC"));
        RedisTokenBucketRateLimiter limiter = new RedisTokenBucketRateLimiter(redisTemplate, fixedClock);

        when(redisTemplate.execute(
                any(RedisScript.class),
                eq(List.of("key1")),
                eq(List.of("10", "60", "1000000", "1"))
        )).thenReturn(Flux.just(1L));

        Boolean result = limiter.allow("key1", 10, 60).block();
        assertTrue(result != null && result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void allow_RedisReturnsZero_ReturnsFalse() {
        Clock fixedClock = Clock.fixed(Instant.ofEpochMilli(1000000L), ZoneId.of("UTC"));
        RedisTokenBucketRateLimiter limiter = new RedisTokenBucketRateLimiter(redisTemplate, fixedClock);

        when(redisTemplate.execute(
                any(RedisScript.class),
                eq(List.of("key1")),
                eq(List.of("10", "60", "1000000", "1"))
        )).thenReturn(Flux.just(0L));

        Boolean result = limiter.allow("key1", 10, 60).block();
        assertFalse(result != null && result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void allow_RedisReturnsEmpty_ReturnsFalse() {
        Clock fixedClock = Clock.fixed(Instant.ofEpochMilli(1000000L), ZoneId.of("UTC"));
        RedisTokenBucketRateLimiter limiter = new RedisTokenBucketRateLimiter(redisTemplate, fixedClock);

        when(redisTemplate.execute(
                any(RedisScript.class),
                eq(List.of("key1")),
                eq(List.of("10", "60", "1000000", "1"))
        )).thenReturn(Flux.empty());

        Boolean result = limiter.allow("key1", 10, 60).block();
        assertFalse(result != null && result);
    }
}
