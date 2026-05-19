package cn.zhongjiating.canteen.user.service;

import cn.zhongjiating.canteen.user.dto.RegisterRequest;
import cn.zhongjiating.canteen.user.dto.UpdateProfileRequest;
import cn.zhongjiating.canteen.user.vo.UserInfoVO;

public interface UserService {

    UserInfoVO register(RegisterRequest request);

    UserInfoVO getProfile(Long userId);

    UserInfoVO updateProfile(Long userId, UpdateProfileRequest request);
}
