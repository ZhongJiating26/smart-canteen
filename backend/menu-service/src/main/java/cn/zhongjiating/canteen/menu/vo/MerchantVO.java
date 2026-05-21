package cn.zhongjiating.canteen.menu.vo;

import cn.zhongjiating.canteen.menu.entity.Merchant;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "商家窗口信息")
public record MerchantVO(
        Long merchantId,
        Long userId,
        String name,
        String contactName,
        String contactPhone,
        String category,
        Integer status,
        LocalDateTime createTime
) {

    public static MerchantVO from(Merchant merchant) {
        return new MerchantVO(merchant.getId(), merchant.getUserId(), merchant.getName(),
                merchant.getContactName(), merchant.getContactPhone(), merchant.getCategory(),
                merchant.getStatus(), merchant.getCreateTime());
    }
}
