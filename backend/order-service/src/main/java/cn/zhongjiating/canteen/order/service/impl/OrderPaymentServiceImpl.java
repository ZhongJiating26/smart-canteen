package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.common.util.SnowflakeIdUtil;
import cn.zhongjiating.canteen.order.dto.PayOrderRequest;
import cn.zhongjiating.canteen.order.dto.PayPasswordVerifyRequest;
import cn.zhongjiating.canteen.order.dto.PayPasswordVerifyResponse;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderConfig;
import cn.zhongjiating.canteen.order.entity.OrderStatusLog;
import cn.zhongjiating.canteen.order.feign.UserFeignClient;
import cn.zhongjiating.canteen.order.mapper.OrderConfigMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mapper.OrderStatusLogMapper;
import cn.zhongjiating.canteen.order.mq.OrderTimeoutMessagePublisher;
import cn.zhongjiating.canteen.order.service.OrderCancelService;
import cn.zhongjiating.canteen.order.service.OrderPaymentService;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderPaymentServiceImpl implements OrderPaymentService {

    private static final int DEFAULT_PAY_TIMEOUT_MINUTES = 10;

    private final OrderMapper orderMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final OrderConfigMapper orderConfigMapper;
    private final UserFeignClient userFeignClient;
    private final OrderCancelService orderCancelService;
    private final OrderTimeoutMessagePublisher timeoutMessagePublisher;

    public OrderPaymentServiceImpl(OrderMapper orderMapper, OrderStatusLogMapper orderStatusLogMapper,
                                   OrderConfigMapper orderConfigMapper, UserFeignClient userFeignClient,
                                   OrderCancelService orderCancelService,
                                   OrderTimeoutMessagePublisher timeoutMessagePublisher) {
        this.orderMapper = orderMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
        this.orderConfigMapper = orderConfigMapper;
        this.userFeignClient = userFeignClient;
        this.orderCancelService = orderCancelService;
        this.timeoutMessagePublisher = timeoutMessagePublisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderStatusVO pay(Long orderId, PayOrderRequest request) {
        UserContext user = requireUser();
        Order order = requireUserOrder(orderId, user.getUserId());
        if (isPayExpired(order)) {
            orderCancelService.cancelExpiredPending(order);
            throw new BusinessException(ErrorCode.ORDER_EXPIRED);
        }
        if (!OrderStatus.PENDING_PAY.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS);
        }
        verifyPayPassword(user.getUserId(), request.payPassword());

        LocalDateTime payTime = LocalDateTime.now();
        int rows = orderMapper.markPaid(order.getId(), payTime);
        if (rows != 1) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS);
        }
        order.setStatus(OrderStatus.PAID.getCode());
        order.setPayTime(payTime);
        writeStatusLog(order, user);
        timeoutMessagePublisher.publishAcceptTimeout(order);
        return OrderStatusVO.from(order);
    }

    private void verifyPayPassword(Long userId, String payPassword) {
        Result<PayPasswordVerifyResponse> result = userFeignClient.verifyPayPassword(
                new PayPasswordVerifyRequest(userId, payPassword));
        if (result == null || result.getCode() == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    result == null || result.getMessage() == null || result.getMessage().isBlank()
                            ? "支付密码验证失败" : result.getMessage());
        }
        if (!result.getData().valid()) {
            throw new BusinessException(ErrorCode.PAY_PASSWORD_ERROR);
        }
    }

    private Order requireUserOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
        }
        if (!userId.equals(order.getUserId())) {
            throw new ForbiddenException();
        }
        return order;
    }

    private boolean isPayExpired(Order order) {
        return OrderStatus.PENDING_PAY.getCode().equals(order.getStatus()) && order.getCreateTime() != null
                && LocalDateTime.now().isAfter(order.getCreateTime().plusMinutes(payTimeoutMinutes()));
    }

    private int payTimeoutMinutes() {
        OrderConfig config = orderConfigMapper.selectOne(new LambdaQueryWrapper<OrderConfig>()
                .eq(OrderConfig::getConfigKey, "pay_timeout_minutes"));
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return DEFAULT_PAY_TIMEOUT_MINUTES;
        }
        try {
            return Math.max(Integer.parseInt(config.getConfigValue()), 1);
        } catch (NumberFormatException ex) {
            return DEFAULT_PAY_TIMEOUT_MINUTES;
        }
    }

    private void writeStatusLog(Order order, UserContext user) {
        OrderStatusLog log = new OrderStatusLog();
        log.setId(SnowflakeIdUtil.nextId());
        log.setOrderId(order.getId());
        log.setFromStatus(OrderStatus.PENDING_PAY.getCode());
        log.setToStatus(OrderStatus.PAID.getCode());
        log.setOperatorId(user.getUserId());
        log.setOperatorRole(user.getRole().getCode());
        log.setRemark("用户支付");
        log.setCreateTime(order.getPayTime());
        orderStatusLogMapper.insert(log);
    }

    private UserContext requireUser() {
        UserContext context = UserContextHolder.require();
        if (!context.isUser()) {
            throw new ForbiddenException();
        }
        return context;
    }
}
