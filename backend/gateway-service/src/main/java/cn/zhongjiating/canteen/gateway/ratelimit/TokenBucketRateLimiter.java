package cn.zhongjiating.canteen.gateway.ratelimit;

import reactor.core.publisher.Mono;

public interface TokenBucketRateLimiter {

    Mono<Boolean> allow(String key, int rate, int windowSeconds);
}
