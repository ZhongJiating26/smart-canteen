package cn.zhongjiating.canteen.common.dto;

import java.io.Serializable;
import java.time.Instant;

public class TokenPair implements Serializable {

    private String accessToken;

    private String refreshToken;

    private String accessJti;

    private String refreshJti;

    private Instant accessExpireAt;

    private Instant refreshExpireAt;

    public TokenPair() {
    }

    public TokenPair(String accessToken, String refreshToken, String accessJti, String refreshJti,
                     Instant accessExpireAt, Instant refreshExpireAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessJti = accessJti;
        this.refreshJti = refreshJti;
        this.accessExpireAt = accessExpireAt;
        this.refreshExpireAt = refreshExpireAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessJti() {
        return accessJti;
    }

    public void setAccessJti(String accessJti) {
        this.accessJti = accessJti;
    }

    public String getRefreshJti() {
        return refreshJti;
    }

    public void setRefreshJti(String refreshJti) {
        this.refreshJti = refreshJti;
    }

    public Instant getAccessExpireAt() {
        return accessExpireAt;
    }

    public void setAccessExpireAt(Instant accessExpireAt) {
        this.accessExpireAt = accessExpireAt;
    }

    public Instant getRefreshExpireAt() {
        return refreshExpireAt;
    }

    public void setRefreshExpireAt(Instant refreshExpireAt) {
        this.refreshExpireAt = refreshExpireAt;
    }
}
