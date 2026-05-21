package cn.zhongjiating.canteen.pickup.service;

import cn.zhongjiating.canteen.pickup.dto.UpdatePickupConfigRequest;
import cn.zhongjiating.canteen.pickup.vo.PickupConfigVO;

import java.util.List;

public interface PickupConfigService {

    List<PickupConfigVO> list();

    PickupConfigVO update(Long operatorId, UpdatePickupConfigRequest request);
}
