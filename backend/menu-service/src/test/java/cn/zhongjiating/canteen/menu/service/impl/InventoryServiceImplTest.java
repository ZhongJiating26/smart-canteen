package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.menu.dto.InventoryItemRequest;
import cn.zhongjiating.canteen.menu.dto.InventoryRequest;
import cn.zhongjiating.canteen.menu.entity.DailyMenuItem;
import cn.zhongjiating.canteen.menu.entity.InventoryLog;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuItemMapper;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuMapper;
import cn.zhongjiating.canteen.menu.mapper.DishMapper;
import cn.zhongjiating.canteen.menu.mapper.InventoryLogMapper;
import cn.zhongjiating.canteen.menu.service.MerchantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private DailyMenuItemMapper itemMapper;
    @Mock
    private InventoryLogMapper inventoryLogMapper;
    @Mock
    private DailyMenuMapper dailyMenuMapper;
    @Mock
    private DishMapper dishMapper;
    @Mock
    private MerchantService merchantService;

    @Test
    void reserveWritesInventoryLogAfterAtomicStockUpdate() {
        InventoryServiceImpl service = service();
        when(itemMapper.selectById(7000001L)).thenReturn(item());
        when(itemMapper.reserve(7000001L, 2)).thenReturn(1);

        service.reserve(request());

        ArgumentCaptor<InventoryLog> logCaptor = ArgumentCaptor.forClass(InventoryLog.class);
        verify(inventoryLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getChangeType()).isEqualTo("RESERVE");
        assertThat(logCaptor.getValue().getBeforeRemaining()).isEqualTo(10);
        assertThat(logCaptor.getValue().getAfterRemaining()).isEqualTo(8);
    }

    @Test
    void reserveRejectsInsufficientStock() {
        InventoryServiceImpl service = service();
        when(itemMapper.selectById(7000001L)).thenReturn(item());
        when(itemMapper.reserve(7000001L, 2)).thenReturn(0);

        assertThatThrownBy(() -> service.reserve(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("库存不足");
    }

    private InventoryServiceImpl service() {
        return new InventoryServiceImpl(itemMapper, inventoryLogMapper, dailyMenuMapper,
                dishMapper, merchantService);
    }

    private InventoryRequest request() {
        return new InventoryRequest(8000001L, List.of(new InventoryItemRequest(7000001L, 2)));
    }

    private DailyMenuItem item() {
        DailyMenuItem item = new DailyMenuItem();
        item.setId(7000001L);
        item.setIsListed(1);
        item.setRemainingStock(10);
        item.setReservedStock(0);
        return item;
    }
}
