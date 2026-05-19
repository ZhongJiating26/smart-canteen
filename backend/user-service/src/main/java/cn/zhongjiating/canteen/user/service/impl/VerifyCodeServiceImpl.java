package cn.zhongjiating.canteen.user.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.user.service.VerifyCodeService;
import cn.zhongjiating.canteen.user.vo.SendCodeVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {

    private static final String FIXED_CODE = "123456";

    private final StringRedisTemplate redisTemplate;

    public VerifyCodeServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public SendCodeVO sendCode(String phone) {
        String limitKey = RedisKeys.smsLimit(phone);
        Boolean firstSend = redisTemplate.opsForValue().setIfAbsent(limitKey, "1", Duration.ofMinutes(1));
        if (Boolean.FALSE.equals(firstSend)) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, "验证码发送过于频繁");
        }
        redisTemplate.opsForValue().set(RedisKeys.sms(phone), FIXED_CODE, Duration.ofMinutes(5));
        return new SendCodeVO(phone, FIXED_CODE);
    }

    @Override
    public void verify(String phone, String verifyCode) {
        String cachedCode = redisTemplate.opsForValue().get(RedisKeys.sms(phone));
        if (cachedCode == null || !cachedCode.equals(verifyCode)) {
            throw new BusinessException(ErrorCode.INVALID_VERIFY_CODE);
        }
    }
}
