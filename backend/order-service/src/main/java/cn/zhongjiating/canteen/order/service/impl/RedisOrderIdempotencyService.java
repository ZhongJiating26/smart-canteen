package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.order.service.OrderIdempotencyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

@Service
public class RedisOrderIdempotencyService implements OrderIdempotencyService {

    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisOrderIdempotencyService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Long findExistingOrderId(Long userId, String idempotentKey, String requestHash) {
        String value = redisTemplate.opsForValue().get(key(userId, idempotentKey));
        if (value == null || value.isBlank()) {
            return null;
        }
        IdempotencyValue existing = read(value);
        if (!requestHash.equals(existing.requestHash())) {
            throw new BusinessException(ErrorCode.DUPLICATE_SUBMIT);
        }
        return existing.orderId();
    }

    @Override
    public void saveAfterCommit(Long userId, String idempotentKey, String requestHash, Long orderId) {
        Runnable writer = () -> redisTemplate.opsForValue().set(key(userId, idempotentKey),
                write(new IdempotencyValue(orderId, requestHash)), TTL);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    writer.run();
                }
            });
            return;
        }
        writer.run();
    }

    private String key(Long userId, String idempotentKey) {
        return "idempotent:order:create:" + userId + ":" + idempotentKey;
    }

    private IdempotencyValue read(String value) {
        try {
            return objectMapper.readValue(value, IdempotencyValue.class);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "订单幂等记录读取失败");
        }
    }

    private String write(IdempotencyValue value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "订单幂等记录写入失败");
        }
    }

    private record IdempotencyValue(Long orderId, String requestHash) {
    }
}
