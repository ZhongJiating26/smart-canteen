package cn.zhongjiating.canteen.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "修改个人资料请求")
public record UpdateProfileRequest(
        @Schema(description = "昵称", example = "张三三")
        @Size(max = 50, message = "昵称最多50个字符")
        String nickname,

        @Schema(description = "头像 URL", example = "https://example.com/avatar.jpg")
        @Size(max = 255, message = "头像地址最多255个字符")
        String avatar,

        @Schema(description = "新手机号。修改手机号时必须同时提供 oldPhoneVerifyCode 和 newPhoneVerifyCode", example = "13900139000")
        @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
        String phone,

        @Schema(description = "学工号。修改学工号时必须提供 studentNoVerifyCode", example = "20260001")
        @Pattern(regexp = "^$|^[A-Za-z0-9]{5,20}$", message = "学工号必须是5-20位字母或数字")
        String studentNo,

        @Schema(description = "旧手机号验证码，修改手机号时必填", example = "123456")
        @Pattern(regexp = "^$|^\\d{4,6}$", message = "旧手机号验证码必须是4-6位数字")
        String oldPhoneVerifyCode,

        @Schema(description = "新手机号验证码，修改手机号时必填", example = "123456")
        @Pattern(regexp = "^$|^\\d{4,6}$", message = "新手机号验证码必须是4-6位数字")
        String newPhoneVerifyCode,

        @Schema(description = "当前手机号验证码，修改学工号时必填", example = "123456")
        @Pattern(regexp = "^$|^\\d{4,6}$", message = "学工号修改验证码必须是4-6位数字")
        String studentNoVerifyCode
) {
}
