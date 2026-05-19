package cn.zhongjiating.canteen.user.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.user.dto.RegisterRequest;
import cn.zhongjiating.canteen.user.dto.UpdateProfileRequest;
import cn.zhongjiating.canteen.user.entity.User;
import cn.zhongjiating.canteen.user.mapper.UserMapper;
import cn.zhongjiating.canteen.user.service.UserService;
import cn.zhongjiating.canteen.user.service.VerifyCodeService;
import cn.zhongjiating.canteen.user.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final String DEMO_VERIFY_CODE = "123456";

    private final UserMapper userMapper;

    private final VerifyCodeService verifyCodeService;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, VerifyCodeService verifyCodeService, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.verifyCodeService = verifyCodeService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO register(RegisterRequest request) {
        String username = request.username().trim();
        if (findByUsername(username) != null) {
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }
        boolean phoneUsername = isPhone(username);
        if (phoneUsername) {
            verifyCodeService.verify(username, request.verifyCode());
        } else if (!DEMO_VERIFY_CODE.equals(request.verifyCode())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFY_CODE);
        }

        User user = new User();
        user.setUsername(username);
        user.setPhone(phoneUsername ? username : null);
        user.setStudentNo(phoneUsername ? null : username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPayPassword(request.payPassword());
        user.setNickname(normalizeNickname(request.nickname(), username));
        user.setRole(RoleEnum.USER.getCode());
        user.setStatus(1);
        userMapper.insert(user);
        return UserInfoVO.from(user);
    }

    @Override
    public UserInfoVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return UserInfoVO.from(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setNickname(request.nickname());
        user.setAvatar(request.avatar());
        userMapper.updateById(user);
        return UserInfoVO.from(user);
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    private boolean isPhone(String username) {
        return username != null && username.matches("^1\\d{10}$");
    }

    private String normalizeNickname(String nickname, String username) {
        if (nickname == null || nickname.isBlank()) {
            return username;
        }
        return nickname.trim();
    }
}
