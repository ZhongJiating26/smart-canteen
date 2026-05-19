package cn.zhongjiating.canteen.common.enums;

public enum VerifyType {

    SCAN("扫码核销"),
    MANUAL("手动输入取餐码核销");

    private final String description;

    VerifyType(String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
