package cn.zhongjiating.canteen.order.mq;

import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderConfig;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RocketMqOrderTimeoutMessagePublisherTest {

    @Mock
    private RocketMQTemplate rocketMQTemplate;
    @Mock
    private RocketMqBrokerAddressOverride brokerAddressOverride;
    @Mock
    private OrderConfigMapper orderConfigMapper;

    @Test
    void payTimeoutUsesConfiguredDeliverTime() {
        when(orderConfigMapper.selectOne(any())).thenReturn(config("3"));
        long beforePublish = System.currentTimeMillis();

        service().publishPayTimeout(order());

        ArgumentCaptor<Message<?>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<Long> deliverTimeCaptor = ArgumentCaptor.forClass(Long.class);
        verify(rocketMQTemplate).syncSendDeliverTimeMills(eq("order-timeout:pay_timeout"),
                messageCaptor.capture(), deliverTimeCaptor.capture());
        assertThat(deliverTimeCaptor.getValue()).isBetween(beforePublish + 180_000L,
                System.currentTimeMillis() + 180_000L);
        assertThat(((OrderTimeoutMessage) messageCaptor.getValue().getPayload()).timeoutType())
                .isEqualTo(RocketMqOrderTimeoutMessagePublisher.PAY_TIMEOUT);
    }

    private RocketMqOrderTimeoutMessagePublisher service() {
        return new RocketMqOrderTimeoutMessagePublisher(rocketMQTemplate, brokerAddressOverride,
                orderConfigMapper, "order-timeout");
    }

    private Order order() {
        Order order = new Order();
        order.setId(8000001L);
        order.setOrderNo("8000001");
        return order;
    }

    private OrderConfig config(String value) {
        OrderConfig config = new OrderConfig();
        config.setConfigKey("pay_timeout_minutes");
        config.setConfigValue(value);
        return config;
    }
}
