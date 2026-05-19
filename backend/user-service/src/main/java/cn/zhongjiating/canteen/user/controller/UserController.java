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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/send-code")
    public Result<SendCodeVO> sendCode(@Valid @RequestBody SendCodeRequest request) {
        return Result.success(verifyCodeService.sendCode(request.phone()));
    }

    @PostMapping("/register")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login/password")
    public Result<LoginVO> loginByPassword(@Valid @RequestBody PasswordLoginRequest request,
                                           HttpServletRequest servletRequest) {
        return Result.success(authService.loginByPassword(request, servletRequest));
    }

    @PostMapping("/login/code")
    public Result<LoginVO> loginByCode(@Valid @RequestBody CodeLoginRequest request,
                                       HttpServletRequest servletRequest) {
        return Result.success(authService.loginByCode(request, servletRequest));
    }

    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) LogoutRequest request,
                               @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        authService.logout(request == null ? new LogoutRequest(null, null) : request, authorizationHeader);
        return Result.success();
    }

    @GetMapping("/profile")
    public Result<UserInfoVO> profile() {
        return Result.success(userService.getProfile(UserContextHolder.requireUserId()));
    }

    @PutMapping("/profile")
    public Result<UserInfoVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.success(userService.updateProfile(UserContextHolder.requireUserId(), request));
    }

    @PostMapping("/verify-pay-password")
    public Result<PayPasswordVerifyVO> verifyPayPassword(@Valid @RequestBody VerifyPayPasswordRequest request) {
        return Result.success(new PayPasswordVerifyVO(payPasswordService.verify(request.userId(), request.payPassword())));
    }
}
