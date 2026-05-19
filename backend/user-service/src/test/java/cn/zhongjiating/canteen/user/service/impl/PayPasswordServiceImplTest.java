package cn.zhongjiating.canteen.user.service.impl;

import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.user.entity.User;
import cn.zhongjiating.canteen.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayPasswordServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    private ValueOperations<String, String> valueOperations;

    private PayPasswordServiceImpl payPasswordService;

    @BeforeEach
    void setUp() {
        valueOperations = mock(ValueOperations.class);
        payPasswordService = new PayPasswordServiceImpl(userMapper, redisTemplate);
    }

    @Test
    void verifyReturnsTrueAndClearsErrorsWhenPasswordMatches() {
        when(redisTemplate.hasKey("pay_lock:1000001")).thenReturn(false);
        when(userMapper.selectById(1000001L)).thenReturn(user());

        boolean verified = payPasswordService.verify(1000001L, "123456");

        assertThat(verified).isTrue();
        verify(redisTemplate).delete("pay_error:1000001");
    }

    @Test
    void verifyLocksAfterFiveErrors() {
        when(redisTemplate.hasKey("pay_lock:1000001")).thenReturn(false);
        when(userMapper.selectById(1000001L)).thenReturn(user());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("pay_error:1000001")).thenReturn(5L);

        assertThatThrownBy(() -> payPasswordService.verify(1000001L, "bad"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("支付密码已锁定，请30分钟后重试");

        verify(redisTemplate).expire("pay_error:1000001", Duration.ofMinutes(30));
        verify(valueOperations).set(eq("pay_lock:1000001"), eq("1"), eq(Duration.ofMinutes(30)));
    }

    private User user() {
        User user = new User();
        user.setId(1000001L);
        user.setPayPassword("123456");
        return user;
    }
}
