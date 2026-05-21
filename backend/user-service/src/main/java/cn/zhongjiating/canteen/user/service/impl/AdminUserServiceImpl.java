package cn.zhongjiating.canteen.user.service.impl;

import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.user.dto.AdminResetPasswordRequest;
import cn.zhongjiating.canteen.user.dto.AdminUserStatusRequest;
import cn.zhongjiating.canteen.user.dto.CreateMerchantAccountRequest;
import cn.zhongjiating.canteen.user.entity.User;
import cn.zhongjiating.canteen.user.mapper.UserMapper;
import cn.zhongjiating.canteen.user.service.AdminUserService;
import cn.zhongjiating.canteen.user.vo.UserInfoVO;
import cn.zhongjiating.canteen.user.vo.MerchantAccountVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final long MAX_PAGE_SIZE = 100L;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<UserInfoVO> pageUsers(long page, long size, String role, Integer status, String keyword) {
        long current = Math.max(page, 1L);
        long pageSize = Math.min(Math.max(size, 1L), MAX_PAGE_SIZE);
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        if (role != null && !role.isBlank()) {
            query.eq(User::getRole, RoleEnum.fromCode(role).getCode());
        }
        if (status != null) {
            query.eq(User::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String like = keyword.trim();
            query.and(wrapper -> wrapper
                    .like(User::getUsername, like)
                    .or()
                    .like(User::getPhone, like)
                    .or()
                    .like(User::getStudentNo, like)
                    .or()
                    .like(User::getNickname, like));
        }
        query.orderByDesc(User::getCreateTime);
        Page<User> userPage = userMapper.selectPage(Page.of(current, pageSize), query);
        List<UserInfoVO> records = userPage.getRecords().stream()
                .map(UserInfoVO::from)
                .toList();
        return PageResult.of(records, userPage.getTotal(), userPage.getSize(), userPage.getCurrent());
    }

    @Override
    public UserInfoVO getUser(Long userId) {
        return UserInfoVO.from(requireUser(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateStatus(Long operatorId, Long userId, AdminUserStatusRequest request) {
        User user = requireUser(userId);
        if (operatorId != null && operatorId.equals(userId) && request.status() == 0) {
            throw new ForbiddenException("管理员不能禁用自己");
        }
        user.setStatus(request.status());
        userMapper.updateById(user);
        return UserInfoVO.from(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, AdminResetPasswordRequest request) {
        User user = requireUser(userId);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantAccountVO createMerchantAccount(CreateMerchantAccountRequest request) {
        String phone = request.phone().trim();
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, phone)) != null
                || userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone)) != null) {
            throw new BusinessException(ErrorCode.USER_EXISTS, "商家登录手机号已存在");
        }
        User user = new User();
        user.setUsername(phone);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPayPassword("123456");
        user.setNickname(normalizeNickname(request.nickname(), phone));
        user.setRole(RoleEnum.MERCHANT.getCode());
        user.setStatus(1);
        userMapper.insert(user);
        return MerchantAccountVO.from(user);
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private String normalizeNickname(String nickname, String username) {
        return nickname == null || nickname.isBlank() ? username : nickname.trim();
    }
}
