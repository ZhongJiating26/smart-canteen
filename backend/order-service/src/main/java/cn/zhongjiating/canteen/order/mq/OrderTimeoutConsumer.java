package cn.zhongjiating.canteen.order.mq;

import cn.zhongjiating.canteen.order.service.OrderTimeoutService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = "${canteen.order.timeout.topic:order-timeout}",
        consumerGroup = "${canteen.order.timeout.consumer-group:order-timeout-consumer}",
        selectorExpression = "pay_timeout || accept_timeout"
)
public class OrderTimeoutConsumer implements RocketMQListener<OrderTimeoutMessage> {

    private final OrderTimeoutService orderTimeoutService;

    public OrderTimeoutConsumer(OrderTimeoutService orderTimeoutService) {
        this.orderTimeoutService = orderTimeoutService;
    }

    @Override
    public void onMessage(OrderTimeoutMessage message) {
        orderTimeoutService.handle(message);
    }
}
