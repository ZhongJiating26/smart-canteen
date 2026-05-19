package cn.zhongjiating.canteen.common.enums;

public enum ResetRule {

    NONE("不重置"),
    DAILY("每天重置"),
    WEEKLY("每周重置");

    private final String description;

    ResetRule(String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
