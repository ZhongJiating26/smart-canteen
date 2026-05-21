package cn.zhongjiating.canteen.menu.service;

import cn.zhongjiating.canteen.menu.vo.AvailableMenuVO;

public interface MenuBrowseService {

    AvailableMenuVO available(Long merchantId, String mealPeriod, Long categoryId);
}
