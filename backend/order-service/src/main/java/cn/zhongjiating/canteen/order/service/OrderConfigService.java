package cn.zhongjiating.canteen.order.service;

import cn.zhongjiating.canteen.order.dto.UpdateOrderConfigRequest;
import cn.zhongjiating.canteen.order.vo.OrderConfigVO;

import java.util.List;

public interface OrderConfigService {

    List<OrderConfigVO> list();

    OrderConfigVO update(Long operatorId, UpdateOrderConfigRequest request);
}
