package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.MealPeriod;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.menu.dto.DailyMenuItemRequest;
import cn.zhongjiating.canteen.menu.dto.SaveDailyMenuRequest;
import cn.zhongjiating.canteen.menu.dto.UpdateDailyMenuItemsRequest;
import cn.zhongjiating.canteen.menu.entity.DailyMenu;
import cn.zhongjiating.canteen.menu.entity.DailyMenuItem;
import cn.zhongjiating.canteen.menu.entity.Dish;
import cn.zhongjiating.canteen.menu.entity.Merchant;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuItemMapper;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuMapper;
import cn.zhongjiating.canteen.menu.mapper.DishMapper;
import cn.zhongjiating.canteen.menu.service.DailyMenuService;
import cn.zhongjiating.canteen.menu.service.MerchantService;
import cn.zhongjiating.canteen.menu.vo.DailyMenuItemVO;
import cn.zhongjiating.canteen.menu.vo.DailyMenuVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DailyMenuServiceImpl implements DailyMenuService {

    private final DailyMenuMapper dailyMenuMapper;
    private final DailyMenuItemMapper itemMapper;
    private final DishMapper dishMapper;
    private final MerchantService merchantService;

    public DailyMenuServiceImpl(DailyMenuMapper dailyMenuMapper, DailyMenuItemMapper itemMapper,
                                DishMapper dishMapper, MerchantService merchantService) {
        this.dailyMenuMapper = dailyMenuMapper;
        this.itemMapper = itemMapper;
        this.dishMapper = dishMapper;
        this.merchantService = merchantService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyMenuVO save(SaveDailyMenuRequest request) {
        Merchant merchant = merchantService.requireCurrentMerchant();
        requireOwnedMerchant(merchant, request.merchantId());
        String period = parsePeriod(request.mealPeriod());
        DailyMenu menu = dailyMenuMapper.selectOne(new LambdaQueryWrapper<DailyMenu>()
                .eq(DailyMenu::getMerchantId, request.merchantId())
                .eq(DailyMenu::getMealPeriod, period));
        if (menu == null) {
            menu = new DailyMenu();
            menu.setMerchantId(request.merchantId());
            menu.setMealPeriod(period);
            menu.setStatus(request.status() == null ? 1 : request.status());
            dailyMenuMapper.insert(menu);
        } else {
            menu.setStatus(request.status() == null ? menu.getStatus() : request.status());
            dailyMenuMapper.updateById(menu);
        }
        replaceItems(merchant.getId(), menu.getId(), request.items());
        return view(menu);
    }

    @Override
    public List<DailyMenuVO> list(Long merchantId, String mealPeriod, Integer status) {
        Merchant merchant = merchantService.requireCurrentMerchant();
        Long ownerId = merchantId == null ? merchant.getId() : requireOwnedMerchant(merchant, merchantId).getId();
        LambdaQueryWrapper<DailyMenu> query = new LambdaQueryWrapper<DailyMenu>()
                .eq(DailyMenu::getMerchantId, ownerId)
                .eq(status != null, DailyMenu::getStatus, status)
                .orderByAsc(DailyMenu::getMealPeriod);
        if (mealPeriod != null && !mealPeriod.isBlank()) {
            query.eq(DailyMenu::getMealPeriod, parsePeriod(mealPeriod));
        }
        return dailyMenuMapper.selectList(query).stream().map(this::view).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyMenuVO updateItems(Long menuId, UpdateDailyMenuItemsRequest request) {
        Merchant merchant = merchantService.requireCurrentMerchant();
        DailyMenu menu = requireMenu(menuId);
        requireOwnedMerchant(merchant, menu.getMerchantId());
        replaceItems(merchant.getId(), menuId, request.items());
        return view(menu);
    }

    private void replaceItems(Long merchantId, Long menuId, List<DailyMenuItemRequest> requests) {
        Set<Long> dishIds = new HashSet<>();
        for (DailyMenuItemRequest request : requests) {
            if (!dishIds.add(request.dishId())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "菜单菜品不能重复");
            }
            Dish dish = dishMapper.selectById(request.dishId());
            if (dish == null || !merchantId.equals(dish.getMerchantId()) || dish.getStatus() != 1) {
                throw new BusinessException(ErrorCode.DISH_NOT_AVAILABLE);
            }
        }
        Map<Long, DailyMenuItem> existingItems = itemMapper.selectList(new LambdaQueryWrapper<DailyMenuItem>()
                        .eq(DailyMenuItem::getDailyMenuId, menuId))
                .stream()
                .collect(Collectors.toMap(DailyMenuItem::getDishId, Function.identity()));
        LocalDate today = LocalDate.now();
        for (DailyMenuItemRequest request : requests) {
            DailyMenuItem item = existingItems.remove(request.dishId());
            if (item == null) {
                item = new DailyMenuItem();
                item.setDailyMenuId(menuId);
                item.setDishId(request.dishId());
                item.setTotalStock(request.dailyStock());
                item.setRemainingStock(request.dailyStock());
                item.setReservedStock(0);
                item.setLastResetDate(today);
                applyMenuItemConfig(item, request);
                itemMapper.insert(item);
                continue;
            }
            applyMenuItemConfig(item, request);
            itemMapper.updateById(item);
        }
        for (DailyMenuItem removedItem : existingItems.values()) {
            if (removedItem.getReservedStock() != null && removedItem.getReservedStock() > 0) {
                removedItem.setIsListed(0);
                itemMapper.updateById(removedItem);
            } else {
                itemMapper.deleteById(removedItem.getId());
            }
        }
    }

    private void applyMenuItemConfig(DailyMenuItem item, DailyMenuItemRequest request) {
        item.setDailyStock(request.dailyStock());
        item.setAlertThreshold(request.alertThreshold());
        item.setIsListed(request.isListed());
    }

    private DailyMenuVO view(DailyMenu menu) {
        List<DailyMenuItemVO> items = itemMapper.selectList(new LambdaQueryWrapper<DailyMenuItem>()
                        .eq(DailyMenuItem::getDailyMenuId, menu.getId())
                        .orderByAsc(DailyMenuItem::getId))
                .stream().map(DailyMenuItemVO::from).toList();
        return DailyMenuVO.of(menu, items);
    }

    private Merchant requireOwnedMerchant(Merchant currentMerchant, Long merchantId) {
        if (!currentMerchant.getId().equals(merchantId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return currentMerchant;
    }

    private DailyMenu requireMenu(Long menuId) {
        DailyMenu menu = dailyMenuMapper.selectById(menuId);
        if (menu == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "每日菜单不存在");
        }
        return menu;
    }

    private String parsePeriod(String mealPeriod) {
        try {
            return MealPeriod.valueOf(mealPeriod.trim().toUpperCase()).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "售卖时段不正确");
        }
    }
}
