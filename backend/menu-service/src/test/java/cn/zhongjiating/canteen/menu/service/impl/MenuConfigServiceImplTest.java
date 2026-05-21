package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.menu.dto.UpdateMenuConfigRequest;
import cn.zhongjiating.canteen.menu.entity.MenuConfig;
import cn.zhongjiating.canteen.menu.mapper.MenuConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuConfigServiceImplTest {

    @Mock
    private MenuConfigMapper menuConfigMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void updateRefreshesRedisAfterSavingInterval() {
        MenuConfigServiceImpl service = new MenuConfigServiceImpl(menuConfigMapper, redisTemplate);
        MenuConfig config = stockAlertConfig();
        when(menuConfigMapper.selectOne(any())).thenReturn(config);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.update(1000003L, new UpdateMenuConfigRequest("stock_alert_scan_interval", "120"));

        verify(menuConfigMapper).updateById(config);
        verify(valueOperations).set("config:menu-service:stock_alert_scan_interval", "120");
    }

    @Test
    void updateRejectsNonPositiveStockAlertInterval() {
        MenuConfigServiceImpl service = new MenuConfigServiceImpl(menuConfigMapper, redisTemplate);
        when(menuConfigMapper.selectOne(any())).thenReturn(stockAlertConfig());

        assertThatThrownBy(() -> service.update(1000003L,
                new UpdateMenuConfigRequest("stock_alert_scan_interval", "0")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("库存预警扫描间隔必须为正整数秒");
    }

    private MenuConfig stockAlertConfig() {
        MenuConfig config = new MenuConfig();
        config.setId(1L);
        config.setConfigKey("stock_alert_scan_interval");
        config.setConfigValue("300");
        config.setConfigType("INT");
        return config;
    }
}
