package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.order.dto.MerchantLookupResponse;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderItem;
import cn.zhongjiating.canteen.order.entity.OrderStatusLog;
import cn.zhongjiating.canteen.order.feign.MenuFeignClient;
import cn.zhongjiating.canteen.order.mapper.OrderItemMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mapper.OrderStatusLogMapper;
import cn.zhongjiating.canteen.order.service.OrderQueryService;
import cn.zhongjiating.canteen.order.vo.OrderDetailVO;
import cn.zhongjiating.canteen.order.vo.OrderItemVO;
import cn.zhongjiating.canteen.order.vo.OrderStatusLogVO;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import cn.zhongjiating.canteen.order.vo.OrderSummaryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    private static final long MAX_PAGE_SIZE = 100L;
    private static final Set<OrderStatus> ACTIVE_MERCHANT_STATUSES =
            Set.of(OrderStatus.PAID, OrderStatus.PREPARING, OrderStatus.READY);

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final MenuFeignClient menuFeignClient;

    public OrderQueryServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                                 OrderStatusLogMapper orderStatusLogMapper, MenuFeignClient menuFeignClient) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
        this.menuFeignClient = menuFeignClient;
    }

    @Override
    public PageResult<OrderSummaryVO> myOrders(long page, long size, String status) {
        UserContext context = requireUser();
        long current = Math.max(page, 1L);
        long pageSize = Math.min(Math.max(size, 1L), MAX_PAGE_SIZE);
        OrderStatus requestedStatus = hasText(status) ? parseStatus(status) : null;
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, context.getUserId())
                .eq(requestedStatus != null, Order::getStatus,
                        requestedStatus == null ? null : requestedStatus.getCode())
                .orderByDesc(Order::getCreateTime);
        Page<Order> orderPage = orderMapper.selectPage(Page.of(current, pageSize), query);
        return PageResult.of(orderPage.getRecords().stream().map(OrderSummaryVO::from).toList(),
                orderPage.getTotal(), orderPage.getSize(), orderPage.getCurrent());
    }

    @Override
    public OrderStatusVO status(Long orderId) {
        return OrderStatusVO.from(requireOwnedUserOrder(orderId));
    }

    @Override
    public OrderDetailVO detail(Long orderId) {
        Order order = requireAccessibleOrder(orderId);
        List<OrderItemVO> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .orderByAsc(OrderItem::getId))
                .stream().map(OrderItemVO::from).toList();
        List<OrderStatusLogVO> timeline = orderStatusLogMapper.selectList(new LambdaQueryWrapper<OrderStatusLog>()
                        .eq(OrderStatusLog::getOrderId, orderId)
                        .orderByAsc(OrderStatusLog::getCreateTime)
                        .orderByAsc(OrderStatusLog::getId))
                .stream().map(OrderStatusLogVO::from).toList();
        return OrderDetailVO.from(order, items, timeline);
    }

    @Override
    public List<OrderSummaryVO> merchantOrders(String status, Long merchantId) {
        MerchantLookupResponse merchant = requireCurrentMerchant();
        if (merchantId != null && !merchant.merchantId().equals(merchantId)) {
            throw new ForbiddenException();
        }
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchant.merchantId())
                .orderByDesc(Order::getCreateTime);
        if (hasText(status)) {
            query.eq(Order::getStatus, parseStatus(status).getCode());
        } else {
            query.in(Order::getStatus, ACTIVE_MERCHANT_STATUSES.stream().map(OrderStatus::getCode).toList());
        }
        return orderMapper.selectList(query).stream().map(OrderSummaryVO::from).toList();
    }

    private Order requireAccessibleOrder(Long orderId) {
        Order order = requireOrder(orderId);
        UserContext context = UserContextHolder.require();
        if (context.isAdmin()) {
            return order;
        }
        if (context.isUser() && context.getUserId().equals(order.getUserId())) {
            return order;
        }
        if (context.isMerchant() && requireCurrentMerchant().merchantId().equals(order.getMerchantId())) {
            return order;
        }
        throw new ForbiddenException();
    }

    private Order requireOwnedUserOrder(Long orderId) {
        UserContext context = requireUser();
        Order order = requireOrder(orderId);
        if (!context.getUserId().equals(order.getUserId())) {
            throw new ForbiddenException();
        }
        return order;
    }

    private Order requireOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
        }
        return order;
    }

    private UserContext requireUser() {
        UserContext context = UserContextHolder.require();
        if (!context.isUser()) {
            throw new ForbiddenException();
        }
        return context;
    }

    private MerchantLookupResponse requireCurrentMerchant() {
        UserContext context = UserContextHolder.require();
        if (!context.isMerchant()) {
            throw new ForbiddenException();
        }
        Result<MerchantLookupResponse> result = menuFeignClient.merchantByUserId(context.getUserId());
        if (result == null || result.getCode() == null || result.getCode() != 200 || result.getData() == null) {
            String message = result == null || !hasText(result.getMessage()) ? "商家信息查询失败" : result.getMessage();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, message);
        }
        MerchantLookupResponse merchant = result.getData();
        if (merchant.status() == null || merchant.status() != 1) {
            throw new ForbiddenException("商家窗口不可用");
        }
        return merchant;
    }

    private OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不正确");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
