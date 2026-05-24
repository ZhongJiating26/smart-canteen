package cn.zhongjiating.canteen.order.task;

import cn.zhongjiating.canteen.order.service.OrderTimeoutService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class OrderTimeoutScanTask {

    private static final String LOCK_KEY = "lock:order:timeout-scan";

    private final OrderTimeoutService orderTimeoutService;
    private final StringRedisTemplate redisTemplate;

    public OrderTimeoutScanTask(OrderTimeoutService orderTimeoutService, StringRedisTemplate redisTemplate) {
        this.orderTimeoutService = orderTimeoutService;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedDelayString = "${canteen.order.timeout.scan-fixed-delay-ms:60000}")
    public void scan() {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, lockValue, 55, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            return;
        }
        try {
            orderTimeoutService.scanExpiredOrders();
        } finally {
            String currentValue = redisTemplate.opsForValue().get(LOCK_KEY);
            if (lockValue.equals(currentValue)) {
                redisTemplate.delete(LOCK_KEY);
            }
        }
    }
}
