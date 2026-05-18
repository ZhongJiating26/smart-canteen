package cn.zhongjiating.canteen.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
@TestExecutionListeners(listeners = {}, mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS)
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
