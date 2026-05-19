package cn.zhongjiating.canteen.common.enums;

public enum OrderStatus {

    PENDING_PAY("待支付"),
    PAID("已下单"),
    PREPARING("制作中"),
    READY("待取餐"),
    COMPLETED("已取餐"),
    CANCELLED("已取消");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
