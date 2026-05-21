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
import static org.mockito.ArgumentMatchers.eq;
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
        RegisterRequest request = new RegisterRequest("13800138000", "20260001", "123456", "654321", "123456", "新用户");
        when(userMapper.selectOne(any())).thenReturn(null);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");

        UserInfoVO userInfo = userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(verifyCodeService).verify("13800138000", "123456");
        verify(userMapper).insert(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("13800138000");
        assertThat(savedUser.getPhone()).isEqualTo("13800138000");
        assertThat(savedUser.getStudentNo()).isEqualTo("20260001");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.getPayPassword()).isEqualTo("654321");
        assertThat(savedUser.getRole()).isEqualTo("USER");
        assertThat(userInfo.username()).isEqualTo("13800138000");
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userMapper.selectOne(any())).thenReturn(new User());
        RegisterRequest request = new RegisterRequest("13800138000", null, "123456", "654321", "123456", "新用户");

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该账号已被注册");

        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void updateProfileVerifiesCodesBeforeChangingPhoneAndStudentNo() {
        User user = user();
        when(userMapper.selectById(1000001L)).thenReturn(user);
        when(userMapper.selectOne(any())).thenReturn(null);

        UserInfoVO userInfo = userService.updateProfile(1000001L,
                new cn.zhongjiating.canteen.user.dto.UpdateProfileRequest(
                        "新昵称",
                        "https://example.com/a.jpg",
                        "13900139000",
                        "20260002",
                        "111111",
                        "222222",
                        "333333"));

        verify(verifyCodeService).verify(eq("13800138000"), eq("111111"));
        verify(verifyCodeService).verify(eq("13900139000"), eq("222222"));
        verify(verifyCodeService).verify(eq("13800138000"), eq("333333"));
        verify(userMapper).updateById(user);
        assertThat(userInfo.username()).isEqualTo("13900139000");
        assertThat(userInfo.phone()).isEqualTo("13900139000");
        assertThat(userInfo.studentNo()).isEqualTo("20260002");
        assertThat(userInfo.nickname()).isEqualTo("新昵称");
    }

    private User user() {
        User user = new User();
        user.setId(1000001L);
        user.setUsername("13800138000");
        user.setPhone("13800138000");
        user.setStudentNo("20260001");
        user.setRole("USER");
        user.setStatus(1);
        return user;
    }
}
