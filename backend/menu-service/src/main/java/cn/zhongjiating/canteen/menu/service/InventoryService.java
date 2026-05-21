package cn.zhongjiating.canteen.menu.service;

import cn.zhongjiating.canteen.menu.dto.InventoryRequest;
import cn.zhongjiating.canteen.menu.vo.InventoryAlertVO;
import cn.zhongjiating.canteen.menu.vo.InventoryResultVO;

import java.util.List;

public interface InventoryService {

    InventoryResultVO reserve(InventoryRequest request);

    InventoryResultVO release(InventoryRequest request);

    InventoryResultVO consume(InventoryRequest request);

    List<InventoryAlertVO> alerts();

    void resetDailyStock();
}
