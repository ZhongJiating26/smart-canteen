package cn.zhongjiating.canteen.common.dto;

import cn.zhongjiating.canteen.common.enums.RoleEnum;

import java.io.Serializable;
import java.time.Instant;

public class JwtClaims implements Serializable {

    private Long userId;

    private String username;

    private RoleEnum role;

    private String jti;

    private String tokenType;

    private Instant issuedAt;

    private Instant expiresAt;

    public JwtClaims() {
    }

    public JwtClaims(Long userId, String username, RoleEnum role, String jti, String tokenType,
                     Instant issuedAt, Instant expiresAt) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.jti = jti;
        this.tokenType = tokenType;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
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

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
