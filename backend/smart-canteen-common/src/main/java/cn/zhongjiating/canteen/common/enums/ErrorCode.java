package cn.zhongjiating.canteen.common.enums;

public enum ErrorCode {

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "Token 无效或已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    RATE_LIMITED(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务内部异常"),

    USER_EXISTS(400, "该账号已被注册"),
    INVALID_CREDENTIALS(401, "账号或密码错误"),
    ACCOUNT_DISABLED(403, "账号已被禁用"),
    INVALID_VERIFY_CODE(400, "验证码无效"),
    INVALID_PASSWORD(400, "密码不符合规则"),
    INVALID_PHONE_FORMAT(400, "手机号格式不正确"),
    PHONE_BOUND(400, "手机号已绑定其他账号"),
    INVALID_STOCK(400, "库存不足"),
    DISH_NOT_AVAILABLE(400, "菜品当前不可购买"),
    ORDER_NOT_EXISTS(404, "订单不存在"),
    ILLEGAL_STATUS(400, "当前状态不可执行此操作"),
    ORDER_CANNOT_CANCEL(400, "当前订单不可取消"),
    PAY_PASSWORD_ERROR(400, "支付密码错误"),
    PAY_PASSWORD_LOCKED(403, "支付密码已锁定，请30分钟后重试"),
    INVALID_PICKUP_CODE(400, "取餐码无效"),
    ORDER_EXPIRED(400, "该订单已取消"),
    WRONG_STALL(400, "请到对应窗口取餐"),
    DUPLICATE_SUBMIT(400, "请勿重复提交");

    private final int httpStatus;

    private final String message;

    ErrorCode(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
