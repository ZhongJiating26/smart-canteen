package cn.zhongjiating.canteen.order.service;

import cn.zhongjiating.canteen.order.mq.OrderTimeoutMessage;

public interface OrderTimeoutService {

    void handle(OrderTimeoutMessage message);

    void scanExpiredOrders();
}
