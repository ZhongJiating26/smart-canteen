package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.MealPeriod;
import cn.zhongjiating.canteen.common.util.OperatingHoursUtil;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.zhongjiating.canteen.menu.feign.OrderFeignClient;

@Service
public class MenuBrowseServiceImpl implements MenuBrowseService {

    private final DailyMenuMapper dailyMenuMapper;
    private final DailyMenuItemMapper itemMapper;
    private final DishMapper dishMapper;
    private final DishCategoryMapper categoryMapper;
    private final MerchantMapper merchantMapper;
    private final OrderFeignClient orderFeignClient;

    public MenuBrowseServiceImpl(DailyMenuMapper dailyMenuMapper, DailyMenuItemMapper itemMapper,
                                 DishMapper dishMapper, DishCategoryMapper categoryMapper,
                                 MerchantMapper merchantMapper, OrderFeignClient orderFeignClient) {
        this.dailyMenuMapper = dailyMenuMapper;
        this.itemMapper = itemMapper;
        this.dishMapper = dishMapper;
        this.categoryMapper = categoryMapper;
        this.merchantMapper = merchantMapper;
        this.orderFeignClient = orderFeignClient;
    }

    @Override
    public AvailableMenuVO available(Long merchantId, String mealPeriod, Long categoryId) {
        MealPeriod period = resolvePeriod(mealPeriod);
        if (period == null) {
            period = MealPeriod.LUNCH;
        }
        List<Merchant> activeMerchants = merchantMapper.selectList(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getStatus, 1)
                .eq(merchantId != null, Merchant::getId, merchantId)
                .orderByAsc(Merchant::getId));
        if (activeMerchants.isEmpty()) {
            return new AvailableMenuVO(List.of());
        }
        Set<Long> merchantIds = activeMerchants.stream().map(Merchant::getId).collect(Collectors.toSet());
        List<DailyMenu> menus = dailyMenuMapper.selectList(new LambdaQueryWrapper<DailyMenu>()
                .eq(DailyMenu::getMealPeriod, period.getCode())
                .eq(DailyMenu::getStatus, 1)
                .in(DailyMenu::getMerchantId, merchantIds));
        if (menus.isEmpty()) {
            menus = dailyMenuMapper.selectList(new LambdaQueryWrapper<DailyMenu>()
                    .eq(DailyMenu::getStatus, 1)
                    .in(DailyMenu::getMerchantId, merchantIds));
        }
        Map<Long, DailyMenu> menusByMerchantId = menus.stream()
                .collect(Collectors.toMap(DailyMenu::getMerchantId, Function.identity(), (m1, m2) -> m1));
        List<DailyMenuItem> items = List.of();
        if (!menus.isEmpty()) {
            Set<Long> menuIds = menus.stream().map(DailyMenu::getId).collect(Collectors.toSet());
            items = itemMapper.selectList(new LambdaQueryWrapper<DailyMenuItem>()
                    .in(DailyMenuItem::getDailyMenuId, menuIds)
                    .eq(DailyMenuItem::getIsListed, 1)
                    .ge(DailyMenuItem::getRemainingStock, 0));
        }
        Map<Long, Dish> dishes = Map.of();
        if (!items.isEmpty()) {
            Set<Long> dishIds = items.stream().map(DailyMenuItem::getDishId).collect(Collectors.toSet());
            dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                            .in(Dish::getId, dishIds)
                            .eq(Dish::getStatus, 1)
                            .eq(categoryId != null, Dish::getCategoryId, categoryId))
                    .stream().collect(Collectors.toMap(Dish::getId, Function.identity()));
        }
        final Map<Long, Dish> finalDishes = dishes;
        final List<DailyMenuItem> finalItems = items.stream().filter(item -> finalDishes.containsKey(item.getDishId())).toList();
        Set<Long> categoryIds = dishes.values().stream().map(Dish::getCategoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, DishCategory> categories = Map.of();
        if (!categoryIds.isEmpty()) {
            categories = categoryMapper.selectList(new LambdaQueryWrapper<DishCategory>()
                            .in(DishCategory::getId, categoryIds)
                            .orderByAsc(DishCategory::getSort))
                    .stream().collect(Collectors.toMap(DishCategory::getId, Function.identity()));
        }
        final Map<Long, DishCategory> finalCategories = categories;
        List<Long> merchantIdsToQuery = activeMerchants.stream().map(Merchant::getId).toList();
        Map<Long, Integer> salesMap = new java.util.HashMap<>();
        try {
            cn.zhongjiating.canteen.common.dto.Result<Map<Long, Integer>> salesResult = orderFeignClient.getMerchantMonthlySales(merchantIdsToQuery);
            if (salesResult != null && salesResult.getCode() == 200 && salesResult.getData() != null) {
                salesMap = salesResult.getData();
            }
        } catch (Exception e) {
            // Ignored to avoid breaking menu browsing
        }

        List<AvailableMenuVO.MerchantMenuVO> merchantMenus = new ArrayList<>();
        for (Merchant merchant : activeMerchants) {
            boolean operating = OperatingHoursUtil.isOperating(merchant.getOperatingHours());
            DailyMenu menu = menusByMerchantId.get(merchant.getId());
            List<AvailableMenuVO.CategoryMenuVO> categoryMenus = List.of();
            if (menu != null) {
                final Long currentMenuId = menu.getId();
                List<DailyMenuItem> menuItems = finalItems.stream()
                        .filter(item -> item.getDailyMenuId().equals(currentMenuId))
                        .toList();
                Map<Long, List<AvailableMenuVO.AvailableDishVO>> catTree = new LinkedHashMap<>();
                for (DailyMenuItem item : menuItems) {
                    Dish dish = finalDishes.get(item.getDishId());
                    if (dish == null || dish.getCategoryId() == null || !finalCategories.containsKey(dish.getCategoryId())) {
                        continue;
                    }
                    catTree.computeIfAbsent(dish.getCategoryId(), ignored -> new ArrayList<>())
                            .add(new AvailableMenuVO.AvailableDishVO(item.getId(), dish.getId(), dish.getName(),
                                    dish.getPrice(), dish.getDescription(), item.getRemainingStock(),
                                    dish.getImage() != null && dish.getImage().length > 0, item.getRemainingStock() > 0));
                }
                categoryMenus = catTree.entrySet().stream()
                        .map(entry -> new AvailableMenuVO.CategoryMenuVO(entry.getKey(), finalCategories.get(entry.getKey()).getName(), entry.getValue()))
                        .toList();
            }
            merchantMenus.add(new AvailableMenuVO.MerchantMenuVO(
                    merchant.getId(),
                    merchant.getName(),
                    operating,
                    merchant.getOperatingHours(),
                    categoryMenus,
                    salesMap.getOrDefault(merchant.getId(), 0)
            ));
        }
        return new AvailableMenuVO(merchantMenus);
    }

    private MealPeriod resolvePeriod(String mealPeriod) {
        if (mealPeriod != null && !mealPeriod.isBlank()) {
            try {
                return MealPeriod.valueOf(mealPeriod.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "售卖时段不正确");
            }
        }
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Shanghai"));
        for (MealPeriod period : MealPeriod.values()) {
            if (period.contains(now)) {
                return period;
            }
        }
        return null;
    }
}
