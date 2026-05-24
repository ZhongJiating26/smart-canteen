package cn.zhongjiating.canteen.order.service;

import cn.zhongjiating.canteen.order.dto.CancelOrderRequest;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;

public interface OrderCancelService {

    OrderStatusVO cancelByUser(Long orderId, CancelOrderRequest request);

    void cancelExpiredPending(Order order);

    void cancelExpiredPaid(Order order);
}
