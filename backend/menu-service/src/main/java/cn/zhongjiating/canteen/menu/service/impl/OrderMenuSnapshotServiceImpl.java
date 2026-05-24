package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.menu.dto.InventoryItemRequest;
import cn.zhongjiating.canteen.menu.dto.OrderMenuSnapshotRequest;
import cn.zhongjiating.canteen.menu.entity.DailyMenu;
import cn.zhongjiating.canteen.menu.entity.DailyMenuItem;
import cn.zhongjiating.canteen.menu.entity.Dish;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuItemMapper;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuMapper;
import cn.zhongjiating.canteen.menu.mapper.DishMapper;
import cn.zhongjiating.canteen.menu.service.MerchantService;
import cn.zhongjiating.canteen.menu.service.OrderMenuSnapshotService;
import cn.zhongjiating.canteen.menu.vo.OrderMenuItemSnapshotVO;
import cn.zhongjiating.canteen.menu.vo.OrderMenuSnapshotVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderMenuSnapshotServiceImpl implements OrderMenuSnapshotService {

    private final DailyMenuItemMapper itemMapper;
    private final DailyMenuMapper dailyMenuMapper;
    private final DishMapper dishMapper;
    private final MerchantService merchantService;

    public OrderMenuSnapshotServiceImpl(DailyMenuItemMapper itemMapper, DailyMenuMapper dailyMenuMapper,
                                        DishMapper dishMapper, MerchantService merchantService) {
        this.itemMapper = itemMapper;
        this.dailyMenuMapper = dailyMenuMapper;
        this.dishMapper = dishMapper;
        this.merchantService = merchantService;
    }

    @Override
    public OrderMenuSnapshotVO snapshot(OrderMenuSnapshotRequest request) {
        merchantService.requireMerchant(request.merchantId());
        Set<Long> itemIds = request.items().stream()
                .map(InventoryItemRequest::dailyMenuItemId)
                .collect(Collectors.toSet());
        Map<Long, DailyMenuItem> items = itemMapper.selectByIds(itemIds).stream()
                .collect(Collectors.toMap(DailyMenuItem::getId, Function.identity()));
        if (items.size() != itemIds.size()) {
            throw new BusinessException(ErrorCode.DISH_NOT_AVAILABLE);
        }
        Map<Long, DailyMenu> menus = dailyMenuMapper.selectByIds(items.values().stream()
                        .map(DailyMenuItem::getDailyMenuId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(DailyMenu::getId, Function.identity()));
        Map<Long, Dish> dishes = dishMapper.selectByIds(items.values().stream()
                        .map(DailyMenuItem::getDishId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Dish::getId, Function.identity()));
        List<OrderMenuItemSnapshotVO> snapshots = items.values().stream()
                .map(item -> buildSnapshot(request.merchantId(), item, menus.get(item.getDailyMenuId()),
                        dishes.get(item.getDishId())))
                .toList();
        return new OrderMenuSnapshotVO(request.merchantId(), snapshots);
    }

    private OrderMenuItemSnapshotVO buildSnapshot(Long merchantId, DailyMenuItem item, DailyMenu menu, Dish dish) {
        if (menu == null || dish == null || item.getIsListed() == null || item.getIsListed() != 1
                || menu.getStatus() == null || menu.getStatus() != 1 || !merchantId.equals(menu.getMerchantId())
                || dish.getStatus() == null || dish.getStatus() != 1 || !merchantId.equals(dish.getMerchantId())) {
            throw new BusinessException(ErrorCode.DISH_NOT_AVAILABLE);
        }
        return OrderMenuItemSnapshotVO.from(item, dish);
    }
}
