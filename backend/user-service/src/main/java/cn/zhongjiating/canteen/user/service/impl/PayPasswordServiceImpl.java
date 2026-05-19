package cn.zhongjiating.canteen.user.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.user.entity.User;
import cn.zhongjiating.canteen.user.mapper.UserMapper;
import cn.zhongjiating.canteen.user.service.PayPasswordService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class PayPasswordServiceImpl implements PayPasswordService {

    private static final int MAX_ERROR_COUNT = 5;

    private final UserMapper userMapper;

    private final StringRedisTemplate redisTemplate;

    public PayPasswordServiceImpl(UserMapper userMapper, StringRedisTemplate redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean verify(Long userId, String payPassword) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeys.payLock(userId)))) {
            throw new BusinessException(ErrorCode.PAY_PASSWORD_LOCKED);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (user.getPayPassword().equals(payPassword)) {
            redisTemplate.delete(RedisKeys.payError(userId));
            return true;
        }
        Long errorCount = redisTemplate.opsForValue().increment(RedisKeys.payError(userId));
        redisTemplate.expire(RedisKeys.payError(userId), Duration.ofMinutes(30));
        if (errorCount != null && errorCount >= MAX_ERROR_COUNT) {
            redisTemplate.opsForValue().set(RedisKeys.payLock(userId), "1", Duration.ofMinutes(30));
            throw new BusinessException(ErrorCode.PAY_PASSWORD_LOCKED);
        }
        throw new BusinessException(ErrorCode.PAY_PASSWORD_ERROR);
    }
}
