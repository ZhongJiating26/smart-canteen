package cn.zhongjiating.canteen.user.service;

import cn.zhongjiating.canteen.user.dto.CodeLoginRequest;
import cn.zhongjiating.canteen.user.dto.LogoutRequest;
import cn.zhongjiating.canteen.user.dto.PasswordLoginRequest;
import cn.zhongjiating.canteen.user.dto.RefreshTokenRequest;
import cn.zhongjiating.canteen.user.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    LoginVO loginByPassword(PasswordLoginRequest request, HttpServletRequest servletRequest);

    LoginVO loginByCode(CodeLoginRequest request, HttpServletRequest servletRequest);

    LoginVO refresh(RefreshTokenRequest request);

    void logout(LogoutRequest request, String authorizationHeader);
}
