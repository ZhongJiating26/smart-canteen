package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisOrderIdempotencyServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    private RedisOrderIdempotencyService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new RedisOrderIdempotencyService(redisTemplate, objectMapper);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void findExistingOrderId_NoValueInRedis_ReturnsNull() {
        when(valueOperations.get(anyString())).thenReturn(null);

        Long result = service.findExistingOrderId(1L, "key", "hash");
        assertNull(result);
    }

    @Test
    void findExistingOrderId_MatchingHash_ReturnsOrderId() throws Exception {
        String json = "{\"orderId\":888,\"requestHash\":\"match_hash\"}";
        when(valueOperations.get("idempotent:order:create:1:key")).thenReturn(json);

        Class<?> clazz = Class.forName("cn.zhongjiating.canteen.order.service.impl.RedisOrderIdempotencyService$IdempotencyValue");
        java.lang.reflect.Constructor<?> constructor = clazz.getDeclaredConstructor(Long.class, String.class);
        constructor.setAccessible(true);
        Object idempotencyValue = constructor.newInstance(888L, "match_hash");

        when(objectMapper.readValue(eq(json), any(Class.class))).thenReturn(idempotencyValue);

        Long result = service.findExistingOrderId(1L, "key", "match_hash");
        assertEquals(888L, result);
    }

    @Test
    void findExistingOrderId_DifferentHash_ThrowsException() throws Exception {
        String json = "{\"orderId\":888,\"requestHash\":\"old_hash\"}";
        when(valueOperations.get("idempotent:order:create:1:key")).thenReturn(json);

        Class<?> clazz = Class.forName("cn.zhongjiating.canteen.order.service.impl.RedisOrderIdempotencyService$IdempotencyValue");
        java.lang.reflect.Constructor<?> constructor = clazz.getDeclaredConstructor(Long.class, String.class);
        constructor.setAccessible(true);
        Object idempotencyValue = constructor.newInstance(888L, "old_hash");

        when(objectMapper.readValue(eq(json), any(Class.class))).thenReturn(idempotencyValue);

        assertThrows(BusinessException.class, () -> service.findExistingOrderId(1L, "key", "new_hash"));
    }

    @Test
    void saveAfterCommit_NoTransaction_WritesImmediately() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("json_value");

        // Ensure no transaction is active
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        service.saveAfterCommit(1L, "key", "hash", 888L);

        verify(valueOperations).set(eq("idempotent:order:create:1:key"), eq("json_value"), eq(Duration.ofMinutes(30)));
    }

    @Test
    void saveAfterCommit_WithActiveTransaction_WritesOnCommit() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("json_value");

        TransactionSynchronizationManager.initSynchronization();
        try {
            TransactionSynchronizationManager.setActualTransactionActive(true);

            service.saveAfterCommit(1L, "key", "hash", 888L);

            // Verify it was not written yet
            verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));

            // Get registered synchronization and trigger it manually
            List<TransactionSynchronization> synchs = TransactionSynchronizationManager.getSynchronizations();
            assertEquals(1, synchs.size());
            synchs.get(0).afterCommit();

            // Verify it writes after commit
            verify(valueOperations).set(eq("idempotent:order:create:1:key"), eq("json_value"), eq(Duration.ofMinutes(30)));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
