package cn.zhongjiating.canteen.order.mq;
 
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderConfig;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
 
import java.time.LocalDateTime;
 
@Component
public class RocketMqOrderTimeoutMessagePublisher implements OrderTimeoutMessagePublisher {
 
    public static final String PAY_TIMEOUT = "pay_timeout";
    public static final String ACCEPT_TIMEOUT = "accept_timeout";
 
    private final RocketMQTemplate rocketMQTemplate;
    private final RocketMqBrokerAddressOverride brokerAddressOverride;
    private final OrderConfigMapper orderConfigMapper;
    private final String topic;
 
    public RocketMqOrderTimeoutMessagePublisher(RocketMQTemplate rocketMQTemplate,
                                                RocketMqBrokerAddressOverride brokerAddressOverride,
                                                OrderConfigMapper orderConfigMapper,
                                                @Value("${canteen.order.timeout.topic:order-timeout}") String topic) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.brokerAddressOverride = brokerAddressOverride;
        this.orderConfigMapper = orderConfigMapper;
        this.topic = topic;
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
        int timeoutMinutes = 10;
        if (PAY_TIMEOUT.equals(timeoutType)) {
            timeoutMinutes = configMinutes("pay_timeout_minutes", 10);
        } else if (ACCEPT_TIMEOUT.equals(timeoutType)) {
            timeoutMinutes = configMinutes("accept_timeout_minutes", 10);
        }
 
        long deliveryTimestamp = System.currentTimeMillis() + (timeoutMinutes * 60L * 1000L);
 
        OrderTimeoutMessage payload = new OrderTimeoutMessage(order.getId(), order.getOrderNo(),
                timeoutType, LocalDateTime.now());
        String destination = destination(timeoutType);
        brokerAddressOverride.prepareTopicRoute(rocketMQTemplate.getProducer(), topic);
 
        Message<OrderTimeoutMessage> message = MessageBuilder
                .withPayload(payload)
                .setHeader("__STARTDELIVERTIME", String.valueOf(deliveryTimestamp))
                .build();
 
        try {
            rocketMQTemplate.syncSend(destination, message, 3000L);
        } catch (RuntimeException ex) {
            if (!brokerAddressOverride.enabled()) {
                throw ex;
            }
            brokerAddressOverride.overrideIfConfigured();
            rocketMQTemplate.syncSend(destination, message, 3000L);
        }
    }
 
    private int configMinutes(String configKey, int defaultValue) {
        try {
            OrderConfig config = orderConfigMapper.selectOne(new LambdaQueryWrapper<OrderConfig>()
                    .eq(OrderConfig::getConfigKey, configKey));
            if (config != null && config.getConfigValue() != null && !config.getConfigValue().isBlank()) {
                return Math.max(Integer.parseInt(config.getConfigValue()), 1);
            }
        } catch (Exception ex) {
            // fallback to default value on mapper or parsing error
        }
        return defaultValue;
    }
 
    private String destination(String tag) {
        return topic + ":" + tag;
    }
}
