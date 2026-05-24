package cn.zhongjiating.canteen.order.service;

import cn.zhongjiating.canteen.order.vo.OrderStatusVO;

public interface OrderMerchantService {

    OrderStatusVO accept(Long orderId);

    OrderStatusVO reject(Long orderId);

    OrderStatusVO ready(Long orderId);
}
