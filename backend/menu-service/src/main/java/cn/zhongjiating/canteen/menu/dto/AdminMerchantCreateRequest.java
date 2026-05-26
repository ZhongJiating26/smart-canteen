package cn.zhongjiating.canteen.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "管理员新增商家窗口请求")
public record AdminMerchantCreateRequest(
        @Schema(description = "商家登录手机号，同时作为 username", example = "13800138002")
        @NotBlank(message = "商家登录手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "商家登录手机号格式不正确")
        String phone,

        @Schema(description = "商家登录密码", example = "123456")
        @NotBlank(message = "商家登录密码不能为空")
        @Size(min = 6, max = 64, message = "商家登录密码长度必须为6-64位")
        String password,

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

        @Schema(description = "营业时间，JSON数组", example = "[{\"startTime\":\"08:00\",\"endTime\":\"10:00\"}]")
        String operatingHours
) {
}
