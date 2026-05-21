package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.InventoryChangeType;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.menu.dto.InventoryItemRequest;
import cn.zhongjiating.canteen.menu.dto.InventoryRequest;
import cn.zhongjiating.canteen.menu.entity.DailyMenu;
import cn.zhongjiating.canteen.menu.entity.DailyMenuItem;
import cn.zhongjiating.canteen.menu.entity.Dish;
import cn.zhongjiating.canteen.menu.entity.InventoryLog;
import cn.zhongjiating.canteen.menu.entity.Merchant;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuItemMapper;
import cn.zhongjiating.canteen.menu.mapper.DailyMenuMapper;
import cn.zhongjiating.canteen.menu.mapper.DishMapper;
import cn.zhongjiating.canteen.menu.mapper.InventoryLogMapper;
import cn.zhongjiating.canteen.menu.service.InventoryService;
import cn.zhongjiating.canteen.menu.service.MerchantService;
import cn.zhongjiating.canteen.menu.vo.InventoryAlertVO;
import cn.zhongjiating.canteen.menu.vo.InventoryResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final DailyMenuItemMapper itemMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final DailyMenuMapper dailyMenuMapper;
    private final DishMapper dishMapper;
    private final MerchantService merchantService;

    public InventoryServiceImpl(DailyMenuItemMapper itemMapper, InventoryLogMapper inventoryLogMapper,
                                DailyMenuMapper dailyMenuMapper,
                                DishMapper dishMapper, MerchantService merchantService) {
        this.itemMapper = itemMapper;
        this.inventoryLogMapper = inventoryLogMapper;
        this.dailyMenuMapper = dailyMenuMapper;
        this.dishMapper = dishMapper;
        this.merchantService = merchantService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InventoryResultVO reserve(InventoryRequest request) {
        for (InventoryItemRequest requestItem : request.items()) {
            DailyMenuItem item = requireItem(requestItem.dailyMenuItemId());
            int rows = itemMapper.reserve(item.getId(), requestItem.quantity());
            if (rows == 0) {
                throw new BusinessException(ErrorCode.INVALID_STOCK);
            }
            writeLog(item, InventoryChangeType.RESERVE, -requestItem.quantity(), item.getRemainingStock(),
                    item.getRemainingStock() - requestItem.quantity(), request.orderId());
        }
        return InventoryResultVO.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InventoryResultVO release(InventoryRequest request) {
        if (hasOrderLog(request.orderId(), InventoryChangeType.RELEASE)) {
            return InventoryResultVO.ok();
        }
        for (InventoryItemRequest requestItem : request.items()) {
            DailyMenuItem item = requireItem(requestItem.dailyMenuItemId());
            int rows = itemMapper.release(item.getId(), requestItem.quantity());
            if (rows == 0) {
                throw new BusinessException(ErrorCode.INVALID_STOCK, "可释放预占库存不足");
            }
            writeLog(item, InventoryChangeType.RELEASE, requestItem.quantity(), item.getRemainingStock(),
                    item.getRemainingStock() + requestItem.quantity(), request.orderId());
        }
        return InventoryResultVO.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InventoryResultVO consume(InventoryRequest request) {
        if (hasOrderLog(request.orderId(), InventoryChangeType.CONSUME)) {
            return InventoryResultVO.ok();
        }
        for (InventoryItemRequest requestItem : request.items()) {
            DailyMenuItem item = requireItem(requestItem.dailyMenuItemId());
            int rows = itemMapper.consume(item.getId(), requestItem.quantity());
            if (rows == 0) {
                throw new BusinessException(ErrorCode.INVALID_STOCK, "可消费预占库存不足");
            }
            writeLog(item, InventoryChangeType.CONSUME, -requestItem.quantity(), item.getRemainingStock(),
                    item.getRemainingStock(), request.orderId());
        }
        return InventoryResultVO.ok();
    }

    @Override
    public List<InventoryAlertVO> alerts() {
        Merchant merchant = merchantService.requireCurrentMerchant();
        Set<Long> menuIds = dailyMenuMapper.selectList(new LambdaQueryWrapper<DailyMenu>()
                        .select(DailyMenu::getId)
                        .eq(DailyMenu::getMerchantId, merchant.getId()))
                .stream().map(DailyMenu::getId).collect(Collectors.toSet());
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<DailyMenuItem> alertItems = itemMapper.selectList(new LambdaQueryWrapper<DailyMenuItem>()
                .in(DailyMenuItem::getDailyMenuId, menuIds)
                .eq(DailyMenuItem::getIsListed, 1)
                .apply("remaining_stock <= alert_threshold"));
        if (alertItems.isEmpty()) {
            return List.of();
        }
        Map<Long, Dish> dishes = dishMapper.selectByIds(alertItems.stream().map(DailyMenuItem::getDishId).toList())
                .stream().collect(Collectors.toMap(Dish::getId, Function.identity()));
        return alertItems.stream()
                .filter(item -> dishes.containsKey(item.getDishId()))
                .map(item -> {
                    Dish dish = dishes.get(item.getDishId());
                    return new InventoryAlertVO(item.getId(), dish.getId(), dish.getName(), dish.getPrice(),
                            item.getRemainingStock(), item.getAlertThreshold());
                })
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyStock() {
        LocalDate today = LocalDate.now();
        List<DailyMenu> activeMenus = dailyMenuMapper.selectList(new LambdaQueryWrapper<DailyMenu>()
                .select(DailyMenu::getId)
                .eq(DailyMenu::getStatus, 1));
        if (activeMenus.isEmpty()) {
            return;
        }
        List<DailyMenuItem> items = itemMapper.selectList(new LambdaQueryWrapper<DailyMenuItem>()
                .in(DailyMenuItem::getDailyMenuId, activeMenus.stream().map(DailyMenu::getId).toList())
                .eq(DailyMenuItem::getIsListed, 1));
        for (DailyMenuItem item : items) {
            if (today.equals(item.getLastResetDate())) {
                continue;
            }
            int before = item.getRemainingStock();
            item.setTotalStock(item.getDailyStock());
            item.setRemainingStock(item.getDailyStock());
            item.setReservedStock(0);
            item.setLastResetDate(today);
            itemMapper.updateById(item);
            writeLog(item, InventoryChangeType.RESET, item.getDailyStock() - before, before,
                    item.getDailyStock(), null);
        }
    }

    private boolean hasOrderLog(Long orderId, InventoryChangeType type) {
        return inventoryLogMapper.selectCount(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getOrderId, orderId)
                .eq(InventoryLog::getChangeType, type.getCode())) > 0;
    }

    private DailyMenuItem requireItem(Long itemId) {
        DailyMenuItem item = itemMapper.selectById(itemId);
        if (item == null || item.getIsListed() == null || item.getIsListed() != 1) {
            throw new BusinessException(ErrorCode.DISH_NOT_AVAILABLE);
        }
        return item;
    }

    private void writeLog(DailyMenuItem item, InventoryChangeType type, int amount,
                          int beforeRemaining, int afterRemaining, Long orderId) {
        InventoryLog log = new InventoryLog();
        log.setDailyMenuItemId(item.getId());
        log.setChangeType(type.getCode());
        log.setChangeAmount(amount);
        log.setBeforeRemaining(beforeRemaining);
        log.setAfterRemaining(afterRemaining);
        log.setOrderId(orderId);
        inventoryLogMapper.insert(log);
    }
}
