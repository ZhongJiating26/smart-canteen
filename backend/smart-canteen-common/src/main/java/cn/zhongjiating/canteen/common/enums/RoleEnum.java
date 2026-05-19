package cn.zhongjiating.canteen.common.enums;

public enum RoleEnum {

    USER("USER", "普通用户"),
    MERCHANT("MERCHANT", "商家"),
    ADMIN("ADMIN", "管理员");

    private final String code;

    private final String description;

    RoleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static RoleEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (RoleEnum value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
