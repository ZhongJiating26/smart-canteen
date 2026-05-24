package cn.zhongjiating.canteen.order.service;

import cn.zhongjiating.canteen.order.dto.CreateOrderRequest;
import cn.zhongjiating.canteen.order.vo.CreateOrderVO;

public interface OrderCreateService {

    CreateOrderVO create(String idempotentKey, CreateOrderRequest request);
}
