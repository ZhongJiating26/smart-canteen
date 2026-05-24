package cn.zhongjiating.canteen.order.service;

import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.order.vo.OrderDetailVO;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import cn.zhongjiating.canteen.order.vo.OrderSummaryVO;

import java.util.List;

public interface OrderQueryService {

    PageResult<OrderSummaryVO> myOrders(long page, long size, String status);

    OrderStatusVO status(Long orderId);

    OrderDetailVO detail(Long orderId);

    List<OrderSummaryVO> merchantOrders(String status, Long merchantId);
}
