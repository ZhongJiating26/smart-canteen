package cn.zhongjiating.canteen.user.controller;

import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.user.dto.CodeLoginRequest;
import cn.zhongjiating.canteen.user.dto.LogoutRequest;
import cn.zhongjiating.canteen.user.dto.PasswordLoginRequest;
import cn.zhongjiating.canteen.user.dto.RefreshTokenRequest;
import cn.zhongjiating.canteen.user.dto.RegisterRequest;
import cn.zhongjiating.canteen.user.dto.SendCodeRequest;
import cn.zhongjiating.canteen.user.dto.UpdateProfileRequest;
import cn.zhongjiating.canteen.user.dto.VerifyPayPasswordRequest;
import cn.zhongjiating.canteen.user.service.AuthService;
import cn.zhongjiating.canteen.user.service.PayPasswordService;
import cn.zhongjiating.canteen.user.service.UserService;
import cn.zhongjiating.canteen.user.service.VerifyCodeService;
import cn.zhongjiating.canteen.user.vo.LoginVO;
import cn.zhongjiating.canteen.user.vo.PayPasswordVerifyVO;
import cn.zhongjiating.canteen.user.vo.SendCodeVO;
import cn.zhongjiating.canteen.user.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户认证与资料", description = "验证码、注册登录、Token、个人资料和支付密码验证")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    private final AuthService authService;

    private final VerifyCodeService verifyCodeService;

    private final PayPasswordService payPasswordService;

    public UserController(UserService userService, AuthService authService, VerifyCodeService verifyCodeService,
                          PayPasswordService payPasswordService) {
        this.userService = userService;
        this.authService = authService;
        this.verifyCodeService = verifyCodeService;
        this.payPasswordService = payPasswordService;
    }

    @Operation(summary = "发送验证码", description = "课程项目固定返回验证码 123456，同一手机号 60 秒内不可重复发送")
    @PostMapping("/send-code")
    public Result<SendCodeVO> sendCode(@Valid @RequestBody SendCodeRequest request) {
        return Result.success(verifyCodeService.sendCode(request.phone()));
    }

    @Operation(summary = "用户注册", description = "手机号必填并作为 username，全局唯一；学工号可为空")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "注册成功",
            content = @Content(examples = @ExampleObject(value = "{\"code\":200,\"message\":\"success\",\"data\":{\"username\":\"13800138000\",\"phone\":\"13800138000\",\"role\":\"USER\"}}")))
    @PostMapping("/register")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @Operation(summary = "密码登录", description = "使用手机号/username 和密码登录，成功后签发 accessToken 与 refreshToken")
    @PostMapping("/login/password")
    public Result<LoginVO> loginByPassword(@Valid @RequestBody PasswordLoginRequest request,
                                           HttpServletRequest servletRequest) {
        return Result.success(authService.loginByPassword(request, servletRequest));
    }

    @Operation(summary = "验证码登录", description = "使用手机号和短信验证码登录")
    @PostMapping("/login/code")
    public Result<LoginVO> loginByCode(@Valid @RequestBody CodeLoginRequest request,
                                       HttpServletRequest servletRequest) {
        return Result.success(authService.loginByCode(request, servletRequest));
    }

    @Operation(summary = "刷新 Token", description = "校验 refreshToken，旧 accessToken 入黑名单，旧 refreshToken 失效并签发新双 Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refresh(request));
    }

    @Operation(summary = "用户登出", description = "当前 accessToken jti 加入黑名单，传入 refreshToken 时同时失效 refreshToken")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) LogoutRequest request,
                               @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        authService.logout(request == null ? new LogoutRequest(null, null) : request, authorizationHeader);
        return Result.success();
    }

    @Operation(summary = "查询个人资料", description = "根据网关注入的 X-User-Id 查询当前登录用户资料")
    @GetMapping("/profile")
    public Result<UserInfoVO> profile() {
        return Result.success(userService.getProfile(UserContextHolder.requireUserId()));
    }

    @Operation(summary = "修改个人资料", description = "可修改昵称、头像、手机号、学工号；修改手机号需要旧手机号和新手机号验证码，修改学工号需要当前手机号验证码")
    @PutMapping("/profile")
    public Result<UserInfoVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.success(userService.updateProfile(UserContextHolder.requireUserId(), request));
    }

    @Operation(summary = "验证支付密码", description = "供 order-service 内部调用，错误 5 次锁定 30 分钟")
    @PostMapping("/verify-pay-password")
    public Result<PayPasswordVerifyVO> verifyPayPassword(@Valid @RequestBody VerifyPayPasswordRequest request) {
        return Result.success(new PayPasswordVerifyVO(payPasswordService.verify(request.userId(), request.payPassword())));
    }
}
