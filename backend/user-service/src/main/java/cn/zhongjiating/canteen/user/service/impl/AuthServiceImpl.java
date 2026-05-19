package cn.zhongjiating.canteen.user.service.impl;

import cn.zhongjiating.canteen.common.dto.JwtClaims;
import cn.zhongjiating.canteen.common.dto.TokenPair;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.common.exception.UnauthorizedException;
import cn.zhongjiating.canteen.common.util.JwtUtil;
import cn.zhongjiating.canteen.user.config.JwtProperties;
import cn.zhongjiating.canteen.user.dto.CodeLoginRequest;
import cn.zhongjiating.canteen.user.dto.LogoutRequest;
import cn.zhongjiating.canteen.user.dto.PasswordLoginRequest;
import cn.zhongjiating.canteen.user.dto.RefreshTokenRequest;
import cn.zhongjiating.canteen.user.entity.LoginLog;
import cn.zhongjiating.canteen.user.entity.RefreshToken;
import cn.zhongjiating.canteen.user.entity.User;
import cn.zhongjiating.canteen.user.mapper.LoginLogMapper;
import cn.zhongjiating.canteen.user.mapper.RefreshTokenMapper;
import cn.zhongjiating.canteen.user.mapper.UserMapper;
import cn.zhongjiating.canteen.user.service.AuthService;
import cn.zhongjiating.canteen.user.service.VerifyCodeService;
import cn.zhongjiating.canteen.user.vo.LoginVO;
import cn.zhongjiating.canteen.user.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    private final LoginLogMapper loginLogMapper;

    private final RefreshTokenMapper refreshTokenMapper;

    private final PasswordEncoder passwordEncoder;

    private final VerifyCodeService verifyCodeService;

    private final StringRedisTemplate redisTemplate;

    private final JwtProperties jwtProperties;

    public AuthServiceImpl(UserMapper userMapper, LoginLogMapper loginLogMapper,
                           RefreshTokenMapper refreshTokenMapper, PasswordEncoder passwordEncoder,
                           VerifyCodeService verifyCodeService, StringRedisTemplate redisTemplate,
                           JwtProperties jwtProperties) {
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.passwordEncoder = passwordEncoder;
        this.verifyCodeService = verifyCodeService;
        this.redisTemplate = redisTemplate;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginByPassword(PasswordLoginRequest request, HttpServletRequest servletRequest) {
        User user = findByUsername(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            writeLoginLog(user, "PASSWORD", servletRequest, 0, "账号或密码错误");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        ensureEnabled(user);
        LoginVO loginVO = issueTokens(user);
        writeLoginLog(user, "PASSWORD", servletRequest, 1, null);
        return loginVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginByCode(CodeLoginRequest request, HttpServletRequest servletRequest) {
        verifyCodeService.verify(request.phone(), request.verifyCode());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, request.phone()));
        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        ensureEnabled(user);
        LoginVO loginVO = issueTokens(user);
        writeLoginLog(user, "VERIFY_CODE", servletRequest, 1, null);
        return loginVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO refresh(RefreshTokenRequest request) {
        JwtClaims claims = JwtUtil.parse(jwtProperties.getSecret(), request.refreshToken());
        if (!JwtUtil.REFRESH_TOKEN.equals(claims.getTokenType())) {
            throw new UnauthorizedException();
        }
        RefreshToken oldRefreshToken = refreshTokenMapper.selectOne(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getTokenJti, claims.getJti()));
        if (oldRefreshToken == null || oldRefreshToken.getStatus() != 1
                || oldRefreshToken.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException();
        }
        String currentRefreshJti = redisTemplate.opsForValue().get(RedisKeys.tokenRefresh(claims.getUserId()));
        if (currentRefreshJti != null && !currentRefreshJti.equals(claims.getJti())) {
            throw new UnauthorizedException();
        }

        blacklistAccessToken(request.accessToken());
        refreshTokenMapper.update(null, new LambdaUpdateWrapper<RefreshToken>()
                .eq(RefreshToken::getTokenJti, claims.getJti())
                .set(RefreshToken::getStatus, 0));

        User user = userMapper.selectById(claims.getUserId());
        if (user == null) {
            throw new UnauthorizedException();
        }
        ensureEnabled(user);
        return issueTokens(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(LogoutRequest request, String authorizationHeader) {
        String accessToken = request.accessToken();
        if ((accessToken == null || accessToken.isBlank()) && authorizationHeader != null) {
            accessToken = authorizationHeader;
        }
        blacklistAccessToken(accessToken);
        if (request.refreshToken() != null && !request.refreshToken().isBlank()) {
            JwtClaims refreshClaims = JwtUtil.parse(jwtProperties.getSecret(), request.refreshToken());
            refreshTokenMapper.update(null, new LambdaUpdateWrapper<RefreshToken>()
                    .eq(RefreshToken::getTokenJti, refreshClaims.getJti())
                    .set(RefreshToken::getStatus, 0));
            redisTemplate.delete(RedisKeys.tokenRefresh(refreshClaims.getUserId()));
        }
    }

    private LoginVO issueTokens(User user) {
        RoleEnum role = RoleEnum.fromCode(user.getRole());
        TokenPair tokenPair = JwtUtil.generateTokenPair(jwtProperties.getSecret(), user.getId(), user.getUsername(),
                role, jwtProperties.getAccessTtl(), jwtProperties.getRefreshTtl());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenJti(tokenPair.getRefreshJti());
        refreshToken.setStatus(1);
        refreshToken.setExpireTime(toLocalDateTime(tokenPair.getRefreshExpireAt()));
        refreshTokenMapper.insert(refreshToken);
        redisTemplate.opsForValue().set(RedisKeys.tokenRefresh(user.getId()), tokenPair.getRefreshJti(),
                jwtProperties.getRefreshTtl());
        return new LoginVO(tokenPair.getAccessToken(), tokenPair.getRefreshToken(), UserInfoVO.from(user));
    }

    private void blacklistAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        JwtClaims claims;
        try {
            claims = JwtUtil.parse(jwtProperties.getSecret(), accessToken);
        } catch (UnauthorizedException ex) {
            return;
        }
        if (!JwtUtil.ACCESS_TOKEN.equals(claims.getTokenType())) {
            return;
        }
        Duration ttl = Duration.between(Instant.now(), claims.getExpiresAt());
        if (!ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.opsForValue().set(RedisKeys.tokenBlacklist(claims.getJti()), "1", ttl);
        }
    }

    private void ensureEnabled(User user) {
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    private void writeLoginLog(User user, String loginType, HttpServletRequest request, int status, String failReason) {
        if (user == null) {
            return;
        }
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(user.getId());
        loginLog.setLoginType(loginType);
        loginLog.setIp(clientIp(request));
        loginLog.setUserAgent(request.getHeader("User-Agent"));
        loginLog.setStatus(status);
        loginLog.setFailReason(failReason);
        loginLogMapper.insert(loginLog);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
