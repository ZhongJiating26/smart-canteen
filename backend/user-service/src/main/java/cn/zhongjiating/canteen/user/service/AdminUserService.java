package cn.zhongjiating.canteen.user.service;

import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.user.dto.AdminResetPasswordRequest;
import cn.zhongjiating.canteen.user.dto.AdminUserStatusRequest;
import cn.zhongjiating.canteen.user.dto.CreateMerchantAccountRequest;
import cn.zhongjiating.canteen.user.vo.MerchantAccountVO;
import cn.zhongjiating.canteen.user.vo.UserInfoVO;

public interface AdminUserService {

    PageResult<UserInfoVO> pageUsers(long page, long size, String role, Integer status, String keyword);

    UserInfoVO getUser(Long userId);

    UserInfoVO updateStatus(Long operatorId, Long userId, AdminUserStatusRequest request);

    void resetPassword(Long userId, AdminResetPasswordRequest request);

    MerchantAccountVO createMerchantAccount(CreateMerchantAccountRequest request);
}
