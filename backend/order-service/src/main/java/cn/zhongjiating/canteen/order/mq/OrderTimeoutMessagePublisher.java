package cn.zhongjiating.canteen.order.mq;

import cn.zhongjiating.canteen.order.entity.Order;

public interface OrderTimeoutMessagePublisher {

    void publishPayTimeout(Order order);

    void publishAcceptTimeout(Order order);
}
