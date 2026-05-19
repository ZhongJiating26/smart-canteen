package cn.zhongjiating.canteen.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50, message = "昵称最多50个字符")
        String nickname,

        @Size(max = 255, message = "头像地址最多255个字符")
        String avatar
) {
}
