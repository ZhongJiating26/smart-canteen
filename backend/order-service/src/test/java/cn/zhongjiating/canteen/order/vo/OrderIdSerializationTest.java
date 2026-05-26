package cn.zhongjiating.canteen.order.vo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderIdSerializationTest {

    private static final long LARGE_ORDER_ID = 273671447547875328L;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesOrderIdsAsStringsForFrontendRoundTrips() throws Exception {
        assertOrderIdIsString(new CreateOrderVO(LARGE_ORDER_ID, String.valueOf(LARGE_ORDER_ID),
                new BigDecimal("24.00"), "PENDING_PAY", null));
        assertOrderIdIsString(new OrderStatusVO(LARGE_ORDER_ID, String.valueOf(LARGE_ORDER_ID),
                "PAID", null, false));
        assertOrderIdIsString(new OrderSummaryVO(LARGE_ORDER_ID, String.valueOf(LARGE_ORDER_ID),
                2000001L, new BigDecimal("24.00"), "PENDING_PAY", null, null));
        assertOrderIdIsString(new OrderDetailVO(LARGE_ORDER_ID, String.valueOf(LARGE_ORDER_ID),
                1000001L, 2000001L, new BigDecimal("24.00"), "PENDING_PAY", null, null, null,
                null, null, null, null, null, null, List.of(), List.of()));
    }

    private void assertOrderIdIsString(Object value) throws Exception {
        JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(value)).get("orderId");

        assertThat(node.isTextual()).isTrue();
        assertThat(node.asText()).isEqualTo(String.valueOf(LARGE_ORDER_ID));
    }
}
