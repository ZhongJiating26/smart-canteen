package cn.zhongjiating.canteen.common.context;

import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.exception.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserContextHolderTest {

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void fromHeadersShouldBuildContext() {
        UserContext context = UserContext.fromHeaders(Map.of(
                "X-User-Id", "1001",
                "X-Username", "student01",
                "X-User-Role", "USER"
        ));

        assertThat(context).isNotNull();
        assertThat(context.getUserId()).isEqualTo(1001L);
        assertThat(context.getUsername()).isEqualTo("student01");
        assertThat(context.getRole()).isEqualTo(RoleEnum.USER);
        assertThat(context.isUser()).isTrue();
    }

    @Test
    void requireShouldFailWhenContextMissing() {
        assertThatThrownBy(UserContextHolder::require)
                .isInstanceOf(UnauthorizedException.class);
    }
}
