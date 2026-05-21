package cn.zhongjiating.canteen.user.vo;

import cn.zhongjiating.canteen.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "用户信息")
public record UserInfoVO(
        @Schema(description = "用户ID", example = "1000001")
        Long userId,
        @Schema(description = "用户名，目前与手机号一致", example = "13800138000")
        String username,
        @Schema(description = "手机号", example = "13800138000")
        String phone,
        @Schema(description = "学工号", example = "20260001")
        String studentNo,
        @Schema(description = "昵称", example = "张三")
        String nickname,
        @Schema(description = "头像 URL", example = "https://example.com/avatar.jpg")
        String avatar,
        @Schema(description = "角色", example = "USER")
        String role,
        @Schema(description = "状态：1=启用，0=禁用", example = "1")
        Integer status,
        @Schema(description = "创建时间", example = "2026-05-21T12:00:00")
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
