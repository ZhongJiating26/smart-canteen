package cn.zhongjiating.canteen.order.service;

import cn.zhongjiating.canteen.order.vo.OrderStatusVO;

public interface OrderCompleteService {

    OrderStatusVO complete(Long orderId);
}
