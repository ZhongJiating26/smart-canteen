package cn.zhongjiating.canteen.gateway.ratelimit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

@Component
public class RedisTokenBucketRateLimiter implements TokenBucketRateLimiter {

    private static final RedisScript<Long> TOKEN_BUCKET_SCRIPT = RedisScript.of("""
            local key = KEYS[1]
            local rate = tonumber(ARGV[1])
            local window = tonumber(ARGV[2]) * 1000
            local now = tonumber(ARGV[3])
            local tokens = tonumber(ARGV[4])
            
            local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
            local current_tokens = tonumber(bucket[1])
            local last_refill = tonumber(bucket[2])
            
            if current_tokens == nil then
                current_tokens = rate
                last_refill = now
            else
                local elapsed = now - last_refill
                local refill = math.floor(elapsed * rate / window)
                if refill > 0 then
                    current_tokens = math.min(rate, current_tokens + refill)
                    last_refill = now
                end
            end
            
            if current_tokens >= tokens then
                current_tokens = current_tokens - tokens
                redis.call('HMSET', key, 'tokens', current_tokens, 'last_refill', last_refill)
                redis.call('EXPIRE', key, window / 1000 * 2)
                return 1
            else
                redis.call('EXPIRE', key, window / 1000 * 2)
                return 0
            end
            """, Long.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final Clock clock;

    @Autowired
    public RedisTokenBucketRateLimiter(ReactiveStringRedisTemplate redisTemplate) {
        this(redisTemplate, Clock.systemUTC());
    }

    RedisTokenBucketRateLimiter(ReactiveStringRedisTemplate redisTemplate, Clock clock) {
        this.redisTemplate = redisTemplate;
        this.clock = clock;
    }

    @Override
    public Mono<Boolean> allow(String key, int rate, int windowSeconds) {
        if (rate <= 0 || windowSeconds <= 0) {
            return Mono.just(false);
        }
        return redisTemplate.execute(TOKEN_BUCKET_SCRIPT, List.of(key),
                        List.of(String.valueOf(rate), String.valueOf(windowSeconds),
                                String.valueOf(clock.millis()), "1"))
                .next()
                .map(result -> result != null && result == 1L)
                .defaultIfEmpty(false);
    }
}
