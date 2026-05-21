package cn.zhongjiating.canteen.menu.vo;

import cn.zhongjiating.canteen.menu.entity.DishCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "菜品分类")
public record CategoryVO(Long categoryId, Long merchantId, String name, Integer sort) {

    public static CategoryVO from(DishCategory category) {
        return new CategoryVO(category.getId(), category.getMerchantId(), category.getName(), category.getSort());
    }
}
