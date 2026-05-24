package cn.zhongjiating.canteen.order.task;

import cn.zhongjiating.canteen.order.service.OrderTimeoutService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTimeoutScanTaskTest {

    @Mock
    private OrderTimeoutService orderTimeoutService;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void scanRunsWhenLockAcquiredAndReleasesOwnLock() {
        AtomicReference<String> lockValue = new AtomicReference<>();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("lock:order:timeout-scan"), anyString(), eq(55L), eq(TimeUnit.SECONDS)))
                .thenAnswer(invocation -> {
                    lockValue.set(invocation.getArgument(1));
                    return true;
                });
        when(valueOperations.get("lock:order:timeout-scan")).thenAnswer(invocation -> lockValue.get());

        OrderTimeoutScanTask task = new OrderTimeoutScanTask(orderTimeoutService, redisTemplate);
        task.scan();

        verify(orderTimeoutService).scanExpiredOrders();
        verify(redisTemplate).delete("lock:order:timeout-scan");
    }

    @Test
    void scanSkipsWhenLockNotAcquired() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("lock:order:timeout-scan"), anyString(), eq(55L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        new OrderTimeoutScanTask(orderTimeoutService, redisTemplate).scan();

        verify(orderTimeoutService, never()).scanExpiredOrders();
    }
}
