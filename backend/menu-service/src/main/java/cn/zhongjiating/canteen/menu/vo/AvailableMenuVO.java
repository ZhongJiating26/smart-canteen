package cn.zhongjiating.canteen.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "游客和用户可浏览菜单")
public record AvailableMenuVO(List<MerchantMenuVO> merchants) {

    public record MerchantMenuVO(Long merchantId, String merchantName, boolean operating, String operatingHours, List<CategoryMenuVO> categories, Integer monthlySales) {
    }

    public record CategoryMenuVO(Long categoryId, String categoryName, List<AvailableDishVO> dishes) {
    }

    public record AvailableDishVO(
            Long dailyMenuItemId,
            Long dishId,
            String dishName,
            BigDecimal price,
            String description,
            Integer remainingStock,
            boolean hasImage,
            boolean isAvailable
    ) {
    }
}
