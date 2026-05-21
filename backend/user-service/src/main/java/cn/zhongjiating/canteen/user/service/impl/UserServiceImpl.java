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
        String phone = request.phone().trim();
        if (findByUsername(phone) != null) {
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }
        if (findByPhone(phone) != null) {
            throw new BusinessException(ErrorCode.PHONE_BOUND);
        }
        String studentNo = normalizeBlank(request.studentNo());
        if (studentNo != null && findByStudentNo(studentNo) != null) {
            throw new BusinessException(ErrorCode.USER_EXISTS, "学工号已被绑定");
        }
        verifyCodeService.verify(phone, request.verifyCode());

        User user = new User();
        user.setUsername(phone);
        user.setPhone(phone);
        user.setStudentNo(studentNo);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPayPassword(request.payPassword());
        user.setNickname(normalizeNickname(request.nickname(), phone));
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
        String nickname = normalizeBlank(request.nickname());
        String avatar = normalizeBlank(request.avatar());
        String newPhone = normalizeBlank(request.phone());
        String newStudentNo = normalizeBlank(request.studentNo());
        boolean changePhone = newPhone != null && !newPhone.equals(user.getPhone());
        boolean changeStudentNo = request.studentNo() != null && !equalsNullable(newStudentNo, user.getStudentNo());

        if (changePhone) {
            verifyPhoneChange(user, newPhone, request.oldPhoneVerifyCode(), request.newPhoneVerifyCode());
        }
        if (changeStudentNo) {
            verifyStudentNoChange(user, newStudentNo, request.studentNoVerifyCode());
        }

        if (request.nickname() != null) {
            user.setNickname(nickname);
        }
        if (request.avatar() != null) {
            user.setAvatar(avatar);
        }
        if (changePhone) {
            user.setUsername(newPhone);
            user.setPhone(newPhone);
        }
        if (changeStudentNo) {
            user.setStudentNo(newStudentNo);
        }
        userMapper.updateById(user);
        return UserInfoVO.from(user);
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    private User findByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    private User findByStudentNo(String studentNo) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getStudentNo, studentNo));
    }

    private String normalizeNickname(String nickname, String username) {
        if (nickname == null || nickname.isBlank()) {
            return username;
        }
        return nickname.trim();
    }

    private void verifyPhoneChange(User user, String newPhone, String oldPhoneVerifyCode, String newPhoneVerifyCode) {
        requireVerifyCode(oldPhoneVerifyCode, "旧手机号验证码不能为空");
        requireVerifyCode(newPhoneVerifyCode, "新手机号验证码不能为空");
        User existing = findByUsername(newPhone);
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.PHONE_BOUND);
        }
        User phoneExisting = findByPhone(newPhone);
        if (phoneExisting != null && !phoneExisting.getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.PHONE_BOUND);
        }
        verifyCodeService.verify(user.getPhone(), oldPhoneVerifyCode);
        verifyCodeService.verify(newPhone, newPhoneVerifyCode);
    }

    private void verifyStudentNoChange(User user, String newStudentNo, String verifyCode) {
        requireVerifyCode(verifyCode, "学工号修改验证码不能为空");
        User existing = newStudentNo == null ? null : findByStudentNo(newStudentNo);
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.USER_EXISTS, "学工号已被绑定");
        }
        verifyCodeService.verify(user.getPhone(), verifyCode);
    }

    private void requireVerifyCode(String verifyCode, String message) {
        if (verifyCode == null || verifyCode.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_VERIFY_CODE, message);
        }
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean equalsNullable(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }
}
