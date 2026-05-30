package cn.zhongjiating.canteen.user.controller;

import cn.zhongjiating.canteen.user.config.UserContextInterceptor;
import cn.zhongjiating.canteen.user.config.WebMvcConfig;
import cn.zhongjiating.canteen.user.dto.AdminResetPasswordRequest;
import cn.zhongjiating.canteen.user.dto.AdminUserStatusRequest;
import cn.zhongjiating.canteen.user.mapper.LoginLogMapper;
import cn.zhongjiating.canteen.user.mapper.RefreshTokenMapper;
import cn.zhongjiating.canteen.user.mapper.UserMapper;
import cn.zhongjiating.canteen.user.service.AdminUserService;
import cn.zhongjiating.canteen.user.vo.UserInfoVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUserController.class)
@Import({UserContextInterceptor.class, WebMvcConfig.class})
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserService adminUserService;

    // Mock MyBatis mappers to override their bean definitions and avoid sqlSessionFactory initialization issues
    @MockBean
    private UserMapper userMapper;

    @MockBean
    private LoginLogMapper loginLogMapper;

    @MockBean
    private RefreshTokenMapper refreshTokenMapper;

    @Test
    void getUser_AsAdmin_ReturnsUserInfo() throws Exception {
        UserInfoVO userInfoVO = new UserInfoVO(1000001L, "admin", "13800138000", "20260001", "AdminUser", null, "ADMIN", 1, null);

        when(adminUserService.getUser(1000001L)).thenReturn(userInfoVO);

        mockMvc.perform(get("/api/admin/users/{userId}", 1000001L)
                        .header("X-User-Id", "1000000")
                        .header("X-User-Role", "ADMIN")
                        .header("X-Username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1000001L))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        verify(adminUserService).getUser(1000001L);
    }

    @Test
    void getUser_AsUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users/{userId}", 1000001L)
                        .header("X-User-Id", "1001")
                        .header("X-User-Role", "USER")
                        .header("X-Username", "user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        verify(adminUserService, never()).getUser(any());
    }

    @Test
    void updateStatus_AsAdmin_ReturnsUpdatedUserInfo() throws Exception {
        AdminUserStatusRequest request = new AdminUserStatusRequest(0);
        UserInfoVO userInfoVO = new UserInfoVO(1000001L, "user1", "13800138000", "20260001", "UserOne", null, "USER", 0, null);

        when(adminUserService.updateStatus(eq(1000000L), eq(1000001L), any(AdminUserStatusRequest.class)))
                .thenReturn(userInfoVO);

        mockMvc.perform(put("/api/admin/users/{userId}/status", 1000001L)
                        .header("X-User-Id", "1000000")
                        .header("X-User-Role", "ADMIN")
                        .header("X-Username", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(0));

        verify(adminUserService).updateStatus(eq(1000000L), eq(1000001L), any(AdminUserStatusRequest.class));
    }

    @Test
    void resetPassword_AsAdmin_ReturnsSuccess() throws Exception {
        AdminResetPasswordRequest request = new AdminResetPasswordRequest("newPassword123");

        doNothing().when(adminUserService).resetPassword(eq(1000001L), any(AdminResetPasswordRequest.class));

        mockMvc.perform(put("/api/admin/users/{userId}/password", 1000001L)
                        .header("X-User-Id", "1000000")
                        .header("X-User-Role", "ADMIN")
                        .header("X-Username", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminUserService).resetPassword(eq(1000001L), any(AdminResetPasswordRequest.class));
    }
}
