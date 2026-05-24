package cn.zhongjiating.canteen.order.service;

import cn.zhongjiating.canteen.order.dto.PayOrderRequest;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;

public interface OrderPaymentService {

    OrderStatusVO pay(Long orderId, PayOrderRequest request);
}
