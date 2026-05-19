package cn.zhongjiating.canteen.user.service.impl;

import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.user.config.JwtProperties;
import cn.zhongjiating.canteen.user.dto.PasswordLoginRequest;
import cn.zhongjiating.canteen.user.entity.LoginLog;
import cn.zhongjiating.canteen.user.entity.RefreshToken;
import cn.zhongjiating.canteen.user.entity.User;
import cn.zhongjiating.canteen.user.mapper.LoginLogMapper;
import cn.zhongjiating.canteen.user.mapper.RefreshTokenMapper;
import cn.zhongjiating.canteen.user.mapper.UserMapper;
import cn.zhongjiating.canteen.user.service.VerifyCodeService;
import cn.zhongjiating.canteen.user.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private LoginLogMapper loginLogMapper;

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerifyCodeService verifyCodeService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HttpServletRequest servletRequest;

    private ValueOperations<String, String> valueOperations;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret");
        jwtProperties.setAccessTtl(Duration.ofHours(2));
        jwtProperties.setRefreshTtl(Duration.ofDays(7));
        valueOperations = mock(ValueOperations.class);
        authService = new AuthServiceImpl(userMapper, loginLogMapper, refreshTokenMapper, passwordEncoder,
                verifyCodeService, redisTemplate, jwtProperties);
    }

    @Test
    void loginByPasswordReturnsTokenPair() {
        User user = user();
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("123456", "hash")).thenReturn(true);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        LoginVO loginVO = authService.loginByPassword(new PasswordLoginRequest("user01", "123456"), servletRequest);

        assertThat(loginVO.accessToken()).isNotBlank();
        assertThat(loginVO.refreshToken()).isNotBlank();
        assertThat(loginVO.userInfo().username()).isEqualTo("user01");
        verify(refreshTokenMapper).insert(any(RefreshToken.class));
        verify(valueOperations).set(eq("token:refresh:1000001"), any(), eq(Duration.ofDays(7)));
        verify(loginLogMapper).insert(any(LoginLog.class));
    }

    @Test
    void loginByPasswordRejectsWrongPassword() {
        User user = user();
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.loginByPassword(new PasswordLoginRequest("user01", "bad"), servletRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("账号或密码错误");
    }

    private User user() {
        User user = new User();
        user.setId(1000001L);
        user.setUsername("user01");
        user.setPasswordHash("hash");
        user.setRole("USER");
        user.setStatus(1);
        return user;
    }
}
