package cn.zhongjiating.canteen.user.service.impl;

import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.user.dto.AdminResetPasswordRequest;
import cn.zhongjiating.canteen.user.dto.AdminUserStatusRequest;
import cn.zhongjiating.canteen.user.entity.User;
import cn.zhongjiating.canteen.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void updateStatusRejectsDisablingSelf() {
        AdminUserServiceImpl service = new AdminUserServiceImpl(userMapper, passwordEncoder);
        when(userMapper.selectById(1000003L)).thenReturn(user(1000003L));

        assertThatThrownBy(() -> service.updateStatus(1000003L, 1000003L, new AdminUserStatusRequest(0)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("管理员不能禁用自己");
    }

    @Test
    void resetPasswordEncodesPassword() {
        AdminUserServiceImpl service = new AdminUserServiceImpl(userMapper, passwordEncoder);
        when(userMapper.selectById(1000001L)).thenReturn(user(1000001L));
        when(passwordEncoder.encode("newPass123")).thenReturn("encoded");

        service.resetPassword(1000001L, new AdminResetPasswordRequest("newPass123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded");
    }

    private User user(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setUsername("13800138000");
        user.setPhone("13800138000");
        user.setRole("USER");
        user.setStatus(1);
        return user;
    }
}
