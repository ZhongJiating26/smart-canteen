package cn.zhongjiating.canteen.user.controller;

import cn.zhongjiating.canteen.user.config.UserContextInterceptor;
import cn.zhongjiating.canteen.user.config.WebMvcConfig;
import cn.zhongjiating.canteen.user.dto.*;
import cn.zhongjiating.canteen.user.mapper.LoginLogMapper;
import cn.zhongjiating.canteen.user.mapper.RefreshTokenMapper;
import cn.zhongjiating.canteen.user.mapper.UserMapper;
import cn.zhongjiating.canteen.user.service.AuthService;
import cn.zhongjiating.canteen.user.service.PayPasswordService;
import cn.zhongjiating.canteen.user.service.UserService;
import cn.zhongjiating.canteen.user.service.VerifyCodeService;
import cn.zhongjiating.canteen.user.vo.LoginVO;
import cn.zhongjiating.canteen.user.vo.SendCodeVO;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({UserContextInterceptor.class, WebMvcConfig.class})
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    @MockBean
    private VerifyCodeService verifyCodeService;

    @MockBean
    private PayPasswordService payPasswordService;

    // Mock MyBatis mappers to override their bean definitions and avoid sqlSessionFactory initialization issues
    @MockBean
    private UserMapper userMapper;

    @MockBean
    private LoginLogMapper loginLogMapper;

    @MockBean
    private RefreshTokenMapper refreshTokenMapper;

    @Test
    void sendCode_ValidPhone_ReturnsSuccess() throws Exception {
        SendCodeRequest request = new SendCodeRequest("13800138000");
        SendCodeVO responseVO = new SendCodeVO("13800138000", "123456");

        when(verifyCodeService.sendCode("13800138000")).thenReturn(responseVO);

        mockMvc.perform(post("/api/user/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.verifyCode").value("123456"));

        verify(verifyCodeService).sendCode("13800138000");
    }

    @Test
    void sendCode_InvalidPhone_ReturnsBadRequest() throws Exception {
        SendCodeRequest request = new SendCodeRequest("12345"); // Invalid phone format

        mockMvc.perform(post("/api/user/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("手机号格式不正确"));

        verify(verifyCodeService, never()).sendCode(any());
    }

    @Test
    void register_ValidRequest_ReturnsSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "13800138000",
                "20260001",
                "password123",
                "123456",
                "123456",
                "TestUser"
        );
        UserInfoVO userInfoVO = new UserInfoVO(1001L, "13800138000", "13800138000", "20260001", "TestUser", null, "USER", 1, null);

        when(userService.register(any(RegisterRequest.class))).thenReturn(userInfoVO);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("13800138000"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void loginByPassword_ValidCredentials_ReturnsTokens() throws Exception {
        PasswordLoginRequest request = new PasswordLoginRequest("13800138000", "password123");
        UserInfoVO userInfoVO = new UserInfoVO(1001L, "13800138000", "13800138000", "20260001", "TestUser", null, "USER", 1, null);
        LoginVO loginVO = new LoginVO("access_token_mock", "refresh_token_mock", userInfoVO);

        when(authService.loginByPassword(any(PasswordLoginRequest.class), any())).thenReturn(loginVO);

        mockMvc.perform(post("/api/user/login/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access_token_mock"));
    }

    @Test
    void getProfile_WithAuthorizedUserHeaders_ReturnsProfile() throws Exception {
        UserInfoVO userInfoVO = new UserInfoVO(1001L, "13800138000", "13800138000", "20260001", "TestUser", null, "USER", 1, null);

        when(userService.getProfile(1001L)).thenReturn(userInfoVO);

        mockMvc.perform(get("/api/user/profile")
                        .header("X-User-Id", "1001")
                        .header("X-User-Role", "USER")
                        .header("X-Username", "13800138000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1001L))
                .andExpect(jsonPath("$.data.nickname").value("TestUser"));

        verify(userService).getProfile(1001L);
    }

    @Test
    void getProfile_WithoutAuthorizedHeaders_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未获取到用户上下文"));
    }
}
