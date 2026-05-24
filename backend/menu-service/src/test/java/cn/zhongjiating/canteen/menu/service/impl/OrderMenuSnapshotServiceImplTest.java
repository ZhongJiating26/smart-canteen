package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.menu.dto.InventoryItemRequest;
import cn.zhongjiating.canteen.menu.dto.OrderMenuSnapshotRequest;
import cn.zhongjiating.canteen.menu.entity.DailyMenu;
import cn.zhongjiating.canteen.menu.entity.DailyMenuItem;
import cn.zhongjiating.canteen.menu.entity.Dish;
import cn.zhongjiating.canteen.menu.entity.Merchant;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuItemMapper;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuMapper;
import cn.zhongjiating.canteen.menu.mapper.DishMapper;
import cn.zhongjiating.canteen.menu.service.MerchantService;
import cn.zhongjiating.canteen.menu.vo.OrderMenuSnapshotVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMenuSnapshotServiceImplTest {

    @Mock
    private DailyMenuItemMapper itemMapper;
    @Mock
    private DailyMenuMapper dailyMenuMapper;
    @Mock
    private DishMapper dishMapper;
    @Mock
    private MerchantService merchantService;

    @Test
    void snapshotReturnsActiveDishSnapshotForMerchant() {
        when(merchantService.requireMerchant(2000001L)).thenReturn(merchant());
        when(itemMapper.selectByIds(any())).thenReturn(List.of(item()));
        when(dailyMenuMapper.selectByIds(any())).thenReturn(List.of(menu(2000001L)));
        when(dishMapper.selectByIds(any())).thenReturn(List.of(dish(2000001L)));

        OrderMenuSnapshotVO snapshot = service().snapshot(request());

        assertThat(snapshot.merchantId()).isEqualTo(2000001L);
        assertThat(snapshot.items()).hasSize(1);
        assertThat(snapshot.items().get(0).dishName()).isEqualTo("番茄鸡蛋盖饭");
        assertThat(snapshot.items().get(0).unitPrice()).isEqualByComparingTo("12.00");
    }

    @Test
    void snapshotRejectsDishFromAnotherMerchant() {
        when(merchantService.requireMerchant(2000001L)).thenReturn(merchant());
        when(itemMapper.selectByIds(any())).thenReturn(List.of(item()));
        when(dailyMenuMapper.selectByIds(any())).thenReturn(List.of(menu(2000001L)));
        when(dishMapper.selectByIds(any())).thenReturn(List.of(dish(2000002L)));

        assertThatThrownBy(() -> service().snapshot(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("菜品当前不可购买");
    }

    private OrderMenuSnapshotServiceImpl service() {
        return new OrderMenuSnapshotServiceImpl(itemMapper, dailyMenuMapper, dishMapper, merchantService);
    }

    private OrderMenuSnapshotRequest request() {
        return new OrderMenuSnapshotRequest(2000001L, List.of(new InventoryItemRequest(7000001L, 2)));
    }

    private Merchant merchant() {
        Merchant merchant = new Merchant();
        merchant.setId(2000001L);
        merchant.setStatus(1);
        return merchant;
    }

    private DailyMenuItem item() {
        DailyMenuItem item = new DailyMenuItem();
        item.setId(7000001L);
        item.setDailyMenuId(6000001L);
        item.setDishId(5000001L);
        item.setIsListed(1);
        return item;
    }

    private DailyMenu menu(Long merchantId) {
        DailyMenu menu = new DailyMenu();
        menu.setId(6000001L);
        menu.setMerchantId(merchantId);
        menu.setStatus(1);
        return menu;
    }

    private Dish dish(Long merchantId) {
        Dish dish = new Dish();
        dish.setId(5000001L);
        dish.setMerchantId(merchantId);
        dish.setName("番茄鸡蛋盖饭");
        dish.setPrice(new BigDecimal("12.00"));
        dish.setStatus(1);
        return dish;
    }
}
