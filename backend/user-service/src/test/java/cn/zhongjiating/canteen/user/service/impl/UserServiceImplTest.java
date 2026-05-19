package cn.zhongjiating.canteen.user.service.impl;

import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.user.dto.RegisterRequest;
import cn.zhongjiating.canteen.user.entity.User;
import cn.zhongjiating.canteen.user.mapper.UserMapper;
import cn.zhongjiating.canteen.user.service.VerifyCodeService;
import cn.zhongjiating.canteen.user.vo.UserInfoVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private VerifyCodeService verifyCodeService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerCreatesUserWithEncodedPassword() {
        RegisterRequest request = new RegisterRequest("13800138000", "123456", "654321", "123456", "新用户");
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");

        UserInfoVO userInfo = userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(verifyCodeService).verify("13800138000", "123456");
        verify(userMapper).insert(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("13800138000");
        assertThat(savedUser.getPhone()).isEqualTo("13800138000");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.getPayPassword()).isEqualTo("654321");
        assertThat(savedUser.getRole()).isEqualTo("USER");
        assertThat(userInfo.username()).isEqualTo("13800138000");
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userMapper.selectOne(any())).thenReturn(new User());
        RegisterRequest request = new RegisterRequest("13800138000", "123456", "654321", "123456", "新用户");

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该账号已被注册");

        verify(userMapper, never()).insert(any(User.class));
    }
}
