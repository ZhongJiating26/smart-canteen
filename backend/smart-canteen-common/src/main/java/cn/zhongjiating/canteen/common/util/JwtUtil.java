package cn.zhongjiating.canteen.common.util;

import cn.zhongjiating.canteen.common.dto.JwtClaims;
import cn.zhongjiating.canteen.common.dto.TokenPair;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public final class JwtUtil {

    public static final String ACCESS_TOKEN = "access_token";

    public static final String REFRESH_TOKEN = "refresh_token";

    private JwtUtil() {
    }

    public static TokenPair generateTokenPair(String secret, Long userId, String username, RoleEnum role) {
        return generateTokenPair(secret, userId, username, role, Duration.ofHours(2), Duration.ofDays(7));
    }

    public static TokenPair generateTokenPair(String secret, Long userId, String username, RoleEnum role,
                                              Duration accessTtl, Duration refreshTtl) {
        Instant now = Instant.now();
        String accessJti = UUID.randomUUID().toString().replace("-", "");
        String refreshJti = UUID.randomUUID().toString().replace("-", "");
        Instant accessExpireAt = now.plus(accessTtl);
        Instant refreshExpireAt = now.plus(refreshTtl);
        String accessToken = generateToken(secret, userId, username, role, accessJti, ACCESS_TOKEN, now, accessExpireAt);
        String refreshToken = generateToken(secret, userId, username, role, refreshJti, REFRESH_TOKEN, now, refreshExpireAt);
        return new TokenPair(accessToken, refreshToken, accessJti, refreshJti, accessExpireAt, refreshExpireAt);
    }

    public static String generateAccessToken(String secret, Long userId, String username, RoleEnum role, Duration ttl) {
        Instant now = Instant.now();
        return generateToken(secret, userId, username, role, UUID.randomUUID().toString().replace("-", ""),
                ACCESS_TOKEN, now, now.plus(ttl));
    }

    public static String generateRefreshToken(String secret, Long userId, String username, RoleEnum role, Duration ttl) {
        Instant now = Instant.now();
        return generateToken(secret, userId, username, role, UUID.randomUUID().toString().replace("-", ""),
                REFRESH_TOKEN, now, now.plus(ttl));
    }

    public static JwtClaims parse(String secret, String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey(secret))
                    .build()
                    .parseSignedClaims(removeBearerPrefix(token))
                    .getPayload();
            return toJwtClaims(claims);
        } catch (Exception ex) {
            throw new UnauthorizedException();
        }
    }

    public static boolean verifySignature(String secret, String token) {
        try {
            parse(secret, token);
            return true;
        } catch (UnauthorizedException ex) {
            return false;
        }
    }

    public static boolean isExpired(String secret, String token) {
        return parse(secret, token).isExpired();
    }

    public static Long getUserId(String secret, String token) {
        return parse(secret, token).getUserId();
    }

    public static RoleEnum getRole(String secret, String token) {
        return parse(secret, token).getRole();
    }

    public static String getUsername(String secret, String token) {
        return parse(secret, token).getUsername();
    }

    public static String getJti(String secret, String token) {
        return parse(secret, token).getJti();
    }

    private static String generateToken(String secret, Long userId, String username, RoleEnum role, String jti,
                                        String tokenType, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .id(jti)
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role.getCode())
                .claim("tokenType", tokenType)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey(secret))
                .compact();
    }

    private static JwtClaims toJwtClaims(Claims claims) {
        Object userIdValue = claims.get("userId");
        Long userId = userIdValue instanceof Number number
                ? number.longValue()
                : Long.valueOf(String.valueOf(userIdValue));
        return new JwtClaims(
                userId,
                claims.get("username", String.class),
                RoleEnum.fromCode(claims.get("role", String.class)),
                claims.getId(),
                claims.get("tokenType", String.class),
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant()
        );
    }

    private static String removeBearerPrefix(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException();
        }
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return token.substring(7).trim();
        }
        return token.trim();
    }

    private static SecretKey secretKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must not be blank");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            bytes = sha256(bytes);
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    private static byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }
}
