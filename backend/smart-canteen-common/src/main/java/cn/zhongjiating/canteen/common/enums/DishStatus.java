package cn.zhongjiating.canteen.common.enums;

public enum DishStatus {

    ACTIVE("启用"),
    DISABLED("停用");

    private final String description;

    DishStatus(String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
