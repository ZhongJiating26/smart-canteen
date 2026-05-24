package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.order.dto.UpdateOrderConfigRequest;
import cn.zhongjiating.canteen.order.entity.OrderConfig;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderConfigServiceImplTest {

    @Mock
    private OrderConfigMapper orderConfigMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void listRefreshesRedisCache() {
        OrderConfigServiceImpl service = new OrderConfigServiceImpl(orderConfigMapper, redisTemplate);
        OrderConfig config = config("pay_timeout_minutes", "10");
        when(orderConfigMapper.selectList(any())).thenReturn(List.of(config));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        assertThat(service.list()).hasSize(1);

        verify(valueOperations).set("config:order-service:pay_timeout_minutes", "10");
    }

    @Test
    void updateRefreshesRedisAfterSavingPositiveMinuteConfig() {
        OrderConfigServiceImpl service = new OrderConfigServiceImpl(orderConfigMapper, redisTemplate);
        OrderConfig config = config("accept_timeout_minutes", "10");
        when(orderConfigMapper.selectOne(any())).thenReturn(config);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.update(1000003L, new UpdateOrderConfigRequest("accept_timeout_minutes", "15"));

        verify(orderConfigMapper).updateById(config);
        verify(valueOperations).set("config:order-service:accept_timeout_minutes", "15");
        assertThat(config.getUpdatedBy()).isEqualTo(1000003L);
    }

    @Test
    void updateRejectsNonPositiveMinuteConfig() {
        OrderConfigServiceImpl service = new OrderConfigServiceImpl(orderConfigMapper, redisTemplate);
        when(orderConfigMapper.selectOne(any())).thenReturn(config("cancel_window_minutes", "1"));

        assertThatThrownBy(() -> service.update(1000003L,
                new UpdateOrderConfigRequest("cancel_window_minutes", "0")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("订单时间配置必须为正整数分钟");
    }

    private OrderConfig config(String key, String value) {
        OrderConfig config = new OrderConfig();
        config.setId(1L);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigType("INT");
        return config;
    }
}
