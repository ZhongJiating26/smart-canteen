package cn.zhongjiating.canteen.menu.service;

import cn.zhongjiating.canteen.menu.dto.SaveDailyMenuRequest;
import cn.zhongjiating.canteen.menu.dto.UpdateDailyMenuItemsRequest;
import cn.zhongjiating.canteen.menu.vo.DailyMenuVO;

import java.util.List;

public interface DailyMenuService {

    DailyMenuVO save(SaveDailyMenuRequest request);

    List<DailyMenuVO> list(Long merchantId, String mealPeriod, Integer status);

    DailyMenuVO updateItems(Long menuId, UpdateDailyMenuItemsRequest request);
}
