package cn.zhongjiating.canteen.pickup.service;

import cn.zhongjiating.canteen.pickup.dto.VerifyPickupRequest;
import cn.zhongjiating.canteen.pickup.vo.VerifyPickupVO;

public interface VerifyPickupService {

    VerifyPickupVO verify(Long operatorId, VerifyPickupRequest request);
}
