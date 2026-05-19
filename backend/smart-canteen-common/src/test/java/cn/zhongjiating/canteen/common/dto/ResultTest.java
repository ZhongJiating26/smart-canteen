package cn.zhongjiating.canteen.common.dto;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void successShouldUseUnifiedShape() {
        Result<String> result = Result.success("ok");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("success");
        assertThat(result.getData()).isEqualTo("ok");
    }

    @Test
    void errorShouldUseErrorCodeStatusAndMessage() {
        Result<Object> result = Result.error(ErrorCode.INVALID_STOCK);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("库存不足");
        assertThat(result.getData()).isNull();
    }
}
