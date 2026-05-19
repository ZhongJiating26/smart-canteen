package cn.zhongjiating.canteen.common.util;

import cn.zhongjiating.canteen.common.dto.JwtClaims;
import cn.zhongjiating.canteen.common.dto.TokenPair;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "smart-canteen-test-secret-should-be-long-enough";

    @Test
    void generateAndParseTokenPair() {
        TokenPair tokenPair = JwtUtil.generateTokenPair(SECRET, 1001L, "student01", RoleEnum.USER);

        JwtClaims accessClaims = JwtUtil.parse(SECRET, tokenPair.getAccessToken());
        JwtClaims refreshClaims = JwtUtil.parse(SECRET, tokenPair.getRefreshToken());

        assertThat(accessClaims.getUserId()).isEqualTo(1001L);
        assertThat(accessClaims.getUsername()).isEqualTo("student01");
        assertThat(accessClaims.getRole()).isEqualTo(RoleEnum.USER);
        assertThat(accessClaims.getJti()).isEqualTo(tokenPair.getAccessJti());
        assertThat(accessClaims.getTokenType()).isEqualTo(JwtUtil.ACCESS_TOKEN);
        assertThat(refreshClaims.getTokenType()).isEqualTo(JwtUtil.REFRESH_TOKEN);
        assertThat(JwtUtil.verifySignature(SECRET, tokenPair.getAccessToken())).isTrue();
    }

    @Test
    void verifySignatureShouldRejectWrongSecret() {
        String token = JwtUtil.generateAccessToken(SECRET, 1001L, "student01", RoleEnum.USER, Duration.ofHours(2));

        assertThat(JwtUtil.verifySignature("another-secret", token)).isFalse();
    }
}
