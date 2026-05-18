package cn.zhongjiating.canteen.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false"
})
@TestExecutionListeners(listeners = {}, mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS)
class GatewayServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
