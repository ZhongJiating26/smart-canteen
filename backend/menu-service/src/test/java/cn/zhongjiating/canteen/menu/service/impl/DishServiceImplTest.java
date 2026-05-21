package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.menu.dto.DishRequest;
import cn.zhongjiating.canteen.menu.entity.Dish;
import cn.zhongjiating.canteen.menu.entity.DishCategory;
import cn.zhongjiating.canteen.menu.entity.Merchant;
import cn.zhongjiating.canteen.menu.mapper.DishCategoryMapper;
import cn.zhongjiating.canteen.menu.mapper.DishMapper;
import cn.zhongjiating.canteen.menu.service.MerchantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishServiceImplTest {

    @Mock
    private DishMapper dishMapper;
    @Mock
    private DishCategoryMapper categoryMapper;
    @Mock
    private MerchantService merchantService;
    @InjectMocks
    private DishServiceImpl dishService;

    @Test
    void createStoresDishUnderCurrentMerchant() {
        when(merchantService.requireCurrentMerchant()).thenReturn(merchant(2000001L));
        when(categoryMapper.selectById(4000001L)).thenReturn(category(4000001L, 2000001L));

        dishService.create(new DishRequest(4000001L, " 番茄鸡蛋盖饭 ", new BigDecimal("12.00"), " 热菜 "));

        ArgumentCaptor<Dish> captor = ArgumentCaptor.forClass(Dish.class);
        verify(dishMapper).insert(captor.capture());
        assertThat(captor.getValue().getMerchantId()).isEqualTo(2000001L);
        assertThat(captor.getValue().getName()).isEqualTo("番茄鸡蛋盖饭");
        assertThat(captor.getValue().getDescription()).isEqualTo("热菜");
        assertThat(captor.getValue().getStatus()).isEqualTo(1);
    }

    @Test
    void updateRejectsForeignCategory() {
        Dish dish = new Dish();
        dish.setId(5000001L);
        dish.setMerchantId(2000001L);
        when(merchantService.requireCurrentMerchant()).thenReturn(merchant(2000001L));
        when(dishMapper.selectById(5000001L)).thenReturn(dish);
        when(categoryMapper.selectById(4000002L)).thenReturn(category(4000002L, 2000002L));

        assertThatThrownBy(() -> dishService.update(5000001L,
                new DishRequest(4000002L, "套餐", new BigDecimal("18.00"), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("菜品分类不存在");
    }

    private Merchant merchant(Long id) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setStatus(1);
        return merchant;
    }

    private DishCategory category(Long id, Long merchantId) {
        DishCategory category = new DishCategory();
        category.setId(id);
        category.setMerchantId(merchantId);
        return category;
    }
}
