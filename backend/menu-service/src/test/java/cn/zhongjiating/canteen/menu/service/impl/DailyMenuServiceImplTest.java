package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.menu.dto.DailyMenuItemRequest;
import cn.zhongjiating.canteen.menu.dto.SaveDailyMenuRequest;
import cn.zhongjiating.canteen.menu.entity.DailyMenu;
import cn.zhongjiating.canteen.menu.entity.DailyMenuItem;
import cn.zhongjiating.canteen.menu.entity.Dish;
import cn.zhongjiating.canteen.menu.entity.Merchant;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuItemMapper;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuMapper;
import cn.zhongjiating.canteen.menu.mapper.DishMapper;
import cn.zhongjiating.canteen.menu.service.MerchantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyMenuServiceImplTest {

    @Mock
    private DailyMenuMapper dailyMenuMapper;
    @Mock
    private DailyMenuItemMapper itemMapper;
    @Mock
    private DishMapper dishMapper;
    @Mock
    private MerchantService merchantService;

    @Test
    void saveInitializesDailyStockForMenuItems() {
        DailyMenuServiceImpl service = new DailyMenuServiceImpl(dailyMenuMapper, itemMapper,
                dishMapper, merchantService);
        when(merchantService.requireCurrentMerchant()).thenReturn(merchant());
        when(dailyMenuMapper.selectOne(any())).thenReturn(null);
        when(dishMapper.selectById(5000001L)).thenReturn(dish());
        when(itemMapper.selectList(any())).thenReturn(List.of());

        service.save(new SaveDailyMenuRequest(2000001L, "LUNCH", 1,
                List.of(new DailyMenuItemRequest(5000001L, 50, 10, 1))));

        ArgumentCaptor<DailyMenuItem> itemCaptor = ArgumentCaptor.forClass(DailyMenuItem.class);
        verify(itemMapper).insert(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getDailyStock()).isEqualTo(50);
        assertThat(itemCaptor.getValue().getRemainingStock()).isEqualTo(50);
        assertThat(itemCaptor.getValue().getReservedStock()).isZero();
    }

    private Merchant merchant() {
        Merchant merchant = new Merchant();
        merchant.setId(2000001L);
        return merchant;
    }

    private Dish dish() {
        Dish dish = new Dish();
        dish.setId(5000001L);
        dish.setMerchantId(2000001L);
        dish.setStatus(1);
        return dish;
    }
}
