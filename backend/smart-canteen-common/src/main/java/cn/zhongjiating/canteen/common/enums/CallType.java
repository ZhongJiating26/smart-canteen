package cn.zhongjiating.canteen.common.enums;

public enum CallType {

    AUTO("出餐自动叫号"),
    MANUAL("商家手动叫号");

    private final String description;

    CallType(String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
