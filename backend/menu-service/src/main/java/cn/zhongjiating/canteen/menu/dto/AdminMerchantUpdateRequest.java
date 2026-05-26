package cn.zhongjiating.canteen.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "管理员修改商家窗口请求")
public record AdminMerchantUpdateRequest(
        @Schema(description = "商家窗口名称", example = "一号窗口")
        @NotBlank(message = "商家窗口名称不能为空")
        @Size(max = 100, message = "商家窗口名称长度不能超过100")
        String name,

        @Schema(description = "负责人姓名", example = "张三")
        @Size(max = 50, message = "负责人姓名长度不能超过50")
        String contactName,

        @Schema(description = "联系电话", example = "13800138002")
        @Pattern(regexp = "^$|^1\\d{10}$", message = "联系电话格式不正确")
        String contactPhone,

        @Schema(description = "经营类型", example = "盖浇饭")
        @Size(max = 50, message = "经营类型长度不能超过50")
        String category,

        @Schema(description = "状态：1=启用，0=停用", example = "1")
        @NotNull(message = "商家窗口状态不能为空")
        @jakarta.validation.constraints.Min(value = 0, message = "商家窗口状态只能为0或1")
        @jakarta.validation.constraints.Max(value = 1, message = "商家窗口状态只能为0或1")
        Integer status,

        @Schema(description = "营业时间，JSON数组", example = "[{\"startTime\":\"08:00\",\"endTime\":\"10:00\"}]")
        String operatingHours
) {
}
