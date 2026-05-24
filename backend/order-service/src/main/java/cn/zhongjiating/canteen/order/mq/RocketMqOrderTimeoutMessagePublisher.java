package cn.zhongjiating.canteen.order.mq;

import cn.zhongjiating.canteen.order.entity.Order;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RocketMqOrderTimeoutMessagePublisher implements OrderTimeoutMessagePublisher {

    public static final String PAY_TIMEOUT = "pay_timeout";
    public static final String ACCEPT_TIMEOUT = "accept_timeout";

    private final RocketMQTemplate rocketMQTemplate;
    private final String topic;
    private final int delayLevel;

    public RocketMqOrderTimeoutMessagePublisher(RocketMQTemplate rocketMQTemplate,
                                                @Value("${canteen.order.timeout.topic:order-timeout}") String topic,
                                                @Value("${canteen.order.timeout.delay-level:14}") int delayLevel) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.topic = topic;
        this.delayLevel = delayLevel;
    }

    @Override
    public void publishPayTimeout(Order order) {
        publish(order, PAY_TIMEOUT);
    }

    @Override
    public void publishAcceptTimeout(Order order) {
        publish(order, ACCEPT_TIMEOUT);
    }

    private void publish(Order order, String timeoutType) {
        OrderTimeoutMessage payload = new OrderTimeoutMessage(order.getId(), order.getOrderNo(),
                timeoutType, LocalDateTime.now());
        rocketMQTemplate.syncSend(destination(timeoutType), MessageBuilder.withPayload(payload).build(),
                3000L, delayLevel);
    }

    private String destination(String tag) {
        return topic + ":" + tag;
    }
}
