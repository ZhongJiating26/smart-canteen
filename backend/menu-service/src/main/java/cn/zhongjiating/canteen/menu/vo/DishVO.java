package cn.zhongjiating.canteen.menu.vo;

import cn.zhongjiating.canteen.menu.entity.Dish;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "菜品信息")
public record DishVO(
        Long dishId,
        Long merchantId,
        Long categoryId,
        String name,
        BigDecimal price,
        String description,
        Integer status,
        boolean hasImage
) {

    public static DishVO from(Dish dish) {
        return new DishVO(dish.getId(), dish.getMerchantId(), dish.getCategoryId(), dish.getName(),
                dish.getPrice(), dish.getDescription(), dish.getStatus(),
                dish.getImage() != null && dish.getImage().length > 0);
    }
}
