package cn.zhongjiating.canteen.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "菜品创建或修改请求")
public record DishRequest(
        @Schema(description = "菜品分类ID", example = "4000001")
        @NotNull(message = "菜品分类不能为空")
        Long categoryId,

        @Schema(description = "菜品名称", example = "番茄鸡蛋盖饭")
        @NotBlank(message = "菜品名称不能为空")
        @Size(max = 100, message = "菜品名称长度不能超过100")
        String name,

        @Schema(description = "价格", example = "12.00")
        @NotNull(message = "菜品价格不能为空")
        @DecimalMin(value = "0.00", message = "菜品价格不能小于0")
        @Digits(integer = 8, fraction = 2, message = "菜品价格最多保留2位小数")
        BigDecimal price,

        @Schema(description = "菜品描述", example = "酸甜开胃")
        @Size(max = 500, message = "菜品描述长度不能超过500")
        String description
) {
}
