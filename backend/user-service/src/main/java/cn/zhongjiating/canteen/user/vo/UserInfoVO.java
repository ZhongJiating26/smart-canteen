package cn.zhongjiating.canteen.user.vo;

import cn.zhongjiating.canteen.user.entity.User;

import java.time.LocalDateTime;

public record UserInfoVO(
        Long userId,
        String username,
        String phone,
        String studentNo,
        String nickname,
        String avatar,
        String role,
        Integer status,
        LocalDateTime createTime
) {

    public static UserInfoVO from(User user) {
        return new UserInfoVO(
                user.getId(),
                user.getUsername(),
                user.getPhone(),
                user.getStudentNo(),
                user.getNickname(),
                user.getAvatar(),
                user.getRole(),
                user.getStatus(),
                user.getCreateTime()
        );
    }
}
