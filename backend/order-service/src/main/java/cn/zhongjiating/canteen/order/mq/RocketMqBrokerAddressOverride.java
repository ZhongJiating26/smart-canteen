package cn.zhongjiating.canteen.order.mq;

import org.apache.rocketmq.client.impl.MQClientManager;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RocketMqBrokerAddressOverride {

    private static final Logger log = LoggerFactory.getLogger(RocketMqBrokerAddressOverride.class);

    private final String brokerName;
    private final String brokerAddr;

    private volatile boolean loggedApplied;
    private volatile boolean loggedFailure;
    private volatile boolean loggedPrepareFailure;

    public RocketMqBrokerAddressOverride(@Value("${canteen.rocketmq.broker-override.broker-name:broker-a}") String brokerName,
                                         @Value("${canteen.rocketmq.broker-override.broker-addr:}") String brokerAddr) {
        this.brokerName = brokerName;
        this.brokerAddr = brokerAddr;
    }

    public boolean enabled() {
        return StringUtils.hasText(brokerName) && StringUtils.hasText(brokerAddr);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        overrideIfConfigured();
    }

    @Scheduled(fixedDelayString = "${canteen.rocketmq.broker-override.refresh-ms:5000}")
    public void refresh() {
        overrideIfConfigured();
    }

    public void overrideIfConfigured() {
        if (!enabled()) {
            return;
        }
        try {
            int count = overrideClientInstances();
            if (!loggedApplied) {
                log.info("RocketMQ broker address override enabled, brokerName={}, brokerAddr={}, clientInstances={}",
                        brokerName, brokerAddr, count);
                loggedApplied = true;
            }
        } catch (RuntimeException ex) {
            if (!loggedFailure) {
                log.warn("Failed to override RocketMQ broker address, brokerName={}, brokerAddr={}",
                        brokerName, brokerAddr, ex);
                loggedFailure = true;
            }
        }
    }

    private final Set<String> preparedTopics = ConcurrentHashMap.newKeySet();

    public void prepareTopicRoute(DefaultMQProducer producer, String topic) {
        if (!enabled() || producer == null || !StringUtils.hasText(topic)) {
            return;
        }
        if (preparedTopics.contains(topic)) {
            return;
        }
        try {
            producer.fetchPublishMessageQueues(topic);
            preparedTopics.add(topic);
        } catch (Exception ex) {
            if (!loggedPrepareFailure) {
                log.warn("Failed to prepare RocketMQ topic route before broker address override, topic={}", topic, ex);
                loggedPrepareFailure = true;
            }
        }
        overrideIfConfigured();
    }

    @SuppressWarnings("unchecked")
    private int overrideClientInstances() {
        Field factoryTableField = ReflectionUtils.findField(MQClientManager.class, "factoryTable");
        if (factoryTableField == null) {
            return 0;
        }
        ReflectionUtils.makeAccessible(factoryTableField);
        Object value = ReflectionUtils.getField(factoryTableField, MQClientManager.getInstance());
        if (!(value instanceof Map<?, ?> factoryTable)) {
            return 0;
        }
        for (Object instance : factoryTable.values()) {
            if (instance instanceof MQClientInstance clientInstance) {
                overrideBrokerAddrTable(clientInstance);
                overrideTopicRouteTable(clientInstance);
            }
        }
        return factoryTable.size();
    }

    @SuppressWarnings("unchecked")
    private void overrideBrokerAddrTable(MQClientInstance clientInstance) {
        Field brokerAddrTableField = ReflectionUtils.findField(MQClientInstance.class, "brokerAddrTable");
        if (brokerAddrTableField == null) {
            return;
        }
        ReflectionUtils.makeAccessible(brokerAddrTableField);
        Object value = ReflectionUtils.getField(brokerAddrTableField, clientInstance);
        if (!(value instanceof ConcurrentMap<?, ?> brokerAddrTable)) {
            return;
        }
        ConcurrentMap<String, HashMap<Long, String>> table =
                (ConcurrentMap<String, HashMap<Long, String>>) brokerAddrTable;
        HashMap<Long, String> brokerAddrs = table.computeIfAbsent(brokerName, ignored -> new HashMap<>());
        synchronized (brokerAddrs) {
            brokerAddrs.put(MixAll.MASTER_ID, brokerAddr);
        }
    }

    @SuppressWarnings("unchecked")
    private void overrideTopicRouteTable(MQClientInstance clientInstance) {
        Field topicRouteTableField = ReflectionUtils.findField(MQClientInstance.class, "topicRouteTable");
        if (topicRouteTableField == null) {
            return;
        }
        ReflectionUtils.makeAccessible(topicRouteTableField);
        Object value = ReflectionUtils.getField(topicRouteTableField, clientInstance);
        if (!(value instanceof ConcurrentMap<?, ?> topicRouteTable)) {
            return;
        }
        ConcurrentMap<String, TopicRouteData> table = (ConcurrentMap<String, TopicRouteData>) topicRouteTable;
        for (TopicRouteData routeData : table.values()) {
            for (BrokerData brokerData : routeData.getBrokerDatas()) {
                if (brokerName.equals(brokerData.getBrokerName())) {
                    brokerData.getBrokerAddrs().put(MixAll.MASTER_ID, brokerAddr);
                }
            }
        }
    }
}
