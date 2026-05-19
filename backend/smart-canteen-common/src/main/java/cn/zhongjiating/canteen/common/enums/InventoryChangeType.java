package cn.zhongjiating.canteen.common.enums;

public enum InventoryChangeType {

    RESERVE("预占库存"),
    RELEASE("释放恢复库存"),
    CONSUME("核销确认扣减");

    private final String description;

    InventoryChangeType(String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
