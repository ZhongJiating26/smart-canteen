package cn.zhongjiating.canteen.common.util;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnowflakeIdUtilTest {

    @Test
    void nextShouldGenerateIncreasingIds() {
        SnowflakeIdUtil idUtil = new SnowflakeIdUtil(1L, 1L);

        long first = idUtil.next();
        long second = idUtil.next();

        assertThat(second).isGreaterThan(first);
    }

    @Test
    void nextIdShouldGenerateUniqueIdsConcurrently() throws InterruptedException {
        int threadCount = 8;
        int idsPerThread = 200;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<Long> ids = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                for (int j = 0; j < idsPerThread; j++) {
                    ids.add(SnowflakeIdUtil.nextId());
                }
                latch.countDown();
            });
        }

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        executor.shutdownNow();
        assertThat(ids).hasSize(threadCount * idsPerThread);
    }

    @Test
    void constructorShouldValidateNodeRange() {
        assertThatThrownBy(() -> new SnowflakeIdUtil(32L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SnowflakeIdUtil(1L, 32L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
