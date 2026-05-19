package cn.zhongjiating.canteen.common.context;

import cn.zhongjiating.canteen.common.enums.RoleEnum;

import java.io.Serializable;
import java.util.Map;

public class UserContext implements Serializable {

    public static final String HEADER_USER_ID = "X-User-Id";

    public static final String HEADER_USER_ROLE = "X-User-Role";

    public static final String HEADER_USERNAME = "X-Username";

    private Long userId;

    private String username;

    private RoleEnum role;

    public UserContext() {
    }

    public UserContext(Long userId, String username, RoleEnum role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public static UserContext of(Long userId, String username, RoleEnum role) {
        return new UserContext(userId, username, role);
    }

    public static UserContext fromHeaders(Map<String, String> headers) {
        if (headers == null) {
            return null;
        }
        String userId = getHeaderIgnoreCase(headers, HEADER_USER_ID);
        String username = getHeaderIgnoreCase(headers, HEADER_USERNAME);
        String role = getHeaderIgnoreCase(headers, HEADER_USER_ROLE);
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return new UserContext(Long.valueOf(userId), username, RoleEnum.fromCode(role));
    }

    private static String getHeaderIgnoreCase(Map<String, String> headers, String name) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean isUser() {
        return RoleEnum.USER == role;
    }

    public boolean isMerchant() {
        return RoleEnum.MERCHANT == role;
    }

    public boolean isAdmin() {
        return RoleEnum.ADMIN == role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }
}
