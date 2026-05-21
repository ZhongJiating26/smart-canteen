package cn.zhongjiating.canteen.user.vo;

import cn.zhongjiating.canteen.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "商家账号创建结果")
public record MerchantAccountVO(
        Long userId,
        String username,
        String role,
        Integer status
) {

    public static MerchantAccountVO from(User user) {
        return new MerchantAccountVO(user.getId(), user.getUsername(), user.getRole(), user.getStatus());
    }
}
