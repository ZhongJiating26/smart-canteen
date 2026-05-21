package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.MealPeriod;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.menu.entity.DailyMenu;
import cn.zhongjiating.canteen.menu.entity.DailyMenuItem;
import cn.zhongjiating.canteen.menu.entity.Dish;
import cn.zhongjiating.canteen.menu.entity.DishCategory;
import cn.zhongjiating.canteen.menu.entity.Merchant;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuItemMapper;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuMapper;
import cn.zhongjiating.canteen.menu.mapper.DishCategoryMapper;
import cn.zhongjiating.canteen.menu.mapper.DishMapper;
import cn.zhongjiating.canteen.menu.mapper.MerchantMapper;
import cn.zhongjiating.canteen.menu.service.MenuBrowseService;
import cn.zhongjiating.canteen.menu.vo.AvailableMenuVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MenuBrowseServiceImpl implements MenuBrowseService {

    private final DailyMenuMapper dailyMenuMapper;
    private final DailyMenuItemMapper itemMapper;
    private final DishMapper dishMapper;
    private final DishCategoryMapper categoryMapper;
    private final MerchantMapper merchantMapper;

    public MenuBrowseServiceImpl(DailyMenuMapper dailyMenuMapper, DailyMenuItemMapper itemMapper,
                                 DishMapper dishMapper, DishCategoryMapper categoryMapper, MerchantMapper merchantMapper) {
        this.dailyMenuMapper = dailyMenuMapper;
        this.itemMapper = itemMapper;
        this.dishMapper = dishMapper;
        this.categoryMapper = categoryMapper;
        this.merchantMapper = merchantMapper;
    }

    @Override
    public AvailableMenuVO available(Long merchantId, String mealPeriod, Long categoryId) {
        MealPeriod period = resolvePeriod(mealPeriod);
        if (period == null) {
            return new AvailableMenuVO(List.of());
        }
        List<DailyMenu> menus = dailyMenuMapper.selectList(new LambdaQueryWrapper<DailyMenu>()
                .eq(DailyMenu::getMealPeriod, period.getCode())
                .eq(DailyMenu::getStatus, 1)
                .eq(merchantId != null, DailyMenu::getMerchantId, merchantId)
                .orderByAsc(DailyMenu::getMerchantId));
        if (menus.isEmpty()) {
            return new AvailableMenuVO(List.of());
        }
        Set<Long> menuIds = menus.stream().map(DailyMenu::getId).collect(Collectors.toSet());
        List<DailyMenuItem> items = itemMapper.selectList(new LambdaQueryWrapper<DailyMenuItem>()
                .in(DailyMenuItem::getDailyMenuId, menuIds)
                .eq(DailyMenuItem::getIsListed, 1)
                .gt(DailyMenuItem::getRemainingStock, 0));
        if (items.isEmpty()) {
            return new AvailableMenuVO(List.of());
        }
        Map<Long, Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                        .in(Dish::getId, items.stream().map(DailyMenuItem::getDishId).collect(Collectors.toSet()))
                        .eq(Dish::getStatus, 1)
                        .eq(categoryId != null, Dish::getCategoryId, categoryId))
                .stream().collect(Collectors.toMap(Dish::getId, Function.identity()));
        items = items.stream().filter(item -> dishes.containsKey(item.getDishId())).toList();
        if (items.isEmpty()) {
            return new AvailableMenuVO(List.of());
        }
        Map<Long, DailyMenu> menusById = menus.stream().collect(Collectors.toMap(DailyMenu::getId, Function.identity()));
        Map<Long, Merchant> merchants = merchantMapper.selectList(new LambdaQueryWrapper<Merchant>()
                        .in(Merchant::getId, menus.stream().map(DailyMenu::getMerchantId).collect(Collectors.toSet()))
                        .eq(Merchant::getStatus, 1))
                .stream().collect(Collectors.toMap(Merchant::getId, Function.identity()));
        Set<Long> categoryIds = dishes.values().stream().map(Dish::getCategoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return new AvailableMenuVO(List.of());
        }
        Map<Long, DishCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<DishCategory>()
                        .in(DishCategory::getId, categoryIds)
                        .orderByAsc(DishCategory::getSort))
                .stream().collect(Collectors.toMap(DishCategory::getId, Function.identity()));
        return new AvailableMenuVO(assemble(items, dishes, menusById, merchants, categories));
    }

    private List<AvailableMenuVO.MerchantMenuVO> assemble(List<DailyMenuItem> items, Map<Long, Dish> dishes,
                                                        Map<Long, DailyMenu> menus, Map<Long, Merchant> merchants,
                                                        Map<Long, DishCategory> categories) {
        Map<Long, Map<Long, List<AvailableMenuVO.AvailableDishVO>>> tree = new LinkedHashMap<>();
        for (DailyMenuItem item : items) {
            DailyMenu menu = menus.get(item.getDailyMenuId());
            Dish dish = dishes.get(item.getDishId());
            if (menu == null || dish == null || dish.getCategoryId() == null
                    || !merchants.containsKey(menu.getMerchantId()) || !categories.containsKey(dish.getCategoryId())) {
                continue;
            }
            tree.computeIfAbsent(menu.getMerchantId(), ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(dish.getCategoryId(), ignored -> new ArrayList<>())
                    .add(new AvailableMenuVO.AvailableDishVO(item.getId(), dish.getId(), dish.getName(),
                            dish.getPrice(), dish.getDescription(), item.getRemainingStock(),
                            dish.getImage() != null && dish.getImage().length > 0, true));
        }
        List<AvailableMenuVO.MerchantMenuVO> result = new ArrayList<>();
        tree.forEach((merchantKey, categoryTree) -> {
            Merchant merchant = merchants.get(merchantKey);
            List<AvailableMenuVO.CategoryMenuVO> categoryMenus = categoryTree.entrySet().stream()
                    .map(entry -> new AvailableMenuVO.CategoryMenuVO(entry.getKey(), categories.get(entry.getKey()).getName(),
                            entry.getValue()))
                    .toList();
            result.add(new AvailableMenuVO.MerchantMenuVO(merchant.getId(), merchant.getName(), categoryMenus));
        });
        return result;
    }

    private MealPeriod resolvePeriod(String mealPeriod) {
        if (mealPeriod != null && !mealPeriod.isBlank()) {
            try {
                return MealPeriod.valueOf(mealPeriod.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "售卖时段不正确");
            }
        }
        LocalTime now = LocalTime.now();
        for (MealPeriod period : MealPeriod.values()) {
            if (period.contains(now)) {
                return period;
            }
        }
        return null;
    }
}
