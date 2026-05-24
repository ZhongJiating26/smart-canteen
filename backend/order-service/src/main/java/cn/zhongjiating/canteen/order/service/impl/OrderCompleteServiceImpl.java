package cn.zhongjiating.canteen.order.service.impl;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.enums.OrderStatus;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.common.util.SnowflakeIdUtil;
import cn.zhongjiating.canteen.order.dto.InventoryItemRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationResponse;
import cn.zhongjiating.canteen.order.entity.Order;
import cn.zhongjiating.canteen.order.entity.OrderItem;
import cn.zhongjiating.canteen.order.entity.OrderStatusLog;
import cn.zhongjiating.canteen.order.feign.MenuFeignClient;
import cn.zhongjiating.canteen.order.mapper.OrderItemMapper;
import cn.zhongjiating.canteen.order.mapper.OrderMapper;
import cn.zhongjiating.canteen.order.mapper.OrderStatusLogMapper;
import cn.zhongjiating.canteen.order.service.OrderCompleteService;
import cn.zhongjiating.canteen.order.vo.OrderStatusVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderCompleteServiceImpl implements OrderCompleteService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final MenuFeignClient menuFeignClient;

    public OrderCompleteServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                                    OrderStatusLogMapper orderStatusLogMapper, MenuFeignClient menuFeignClient) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
        this.menuFeignClient = menuFeignClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderStatusVO complete(Long orderId) {
        Order order = requireOrder(orderId);
        if (OrderStatus.COMPLETED.getCode().equals(order.getStatus())) {
            return OrderStatusVO.from(order);
        }
        if (!OrderStatus.READY.getCode().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS);
        }

        consumeInventory(orderId);

        LocalDateTime completeTime = LocalDateTime.now();
        int rows = orderMapper.markCompleted(orderId, completeTime);
        if (rows != 1) {
            Order latest = requireOrder(orderId);
            if (OrderStatus.COMPLETED.getCode().equals(latest.getStatus())) {
                return OrderStatusVO.from(latest);
            }
            throw new BusinessException(ErrorCode.ILLEGAL_STATUS);
        }
        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setCompleteTime(completeTime);
        writeStatusLog(order, completeTime);
        return OrderStatusVO.from(order);
    }

    private void consumeInventory(Long orderId) {
        List<InventoryItemRequest> items = orderItemMapper.selectByOrderId(orderId)
                .stream()
                .map(this::toInventoryItem)
                .toList();
        if (items.isEmpty() || items.stream().anyMatch(item -> item.dailyMenuItemId() == null)) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "订单明细缺少库存消费项");
        }
        Result<InventoryOperationResponse> result = menuFeignClient.consume(new InventoryOperationRequest(orderId, items));
        if (result == null || result.getCode() == null || result.getCode() != 200 || result.getData() == null
                || !result.getData().success()) {
            throw remoteFailure(result, "库存消费失败");
        }
    }

    private InventoryItemRequest toInventoryItem(OrderItem item) {
        return new InventoryItemRequest(item.getDailyMenuItemId(), item.getQuantity());
    }

    private Order requireOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EXISTS);
        }
        return order;
    }

    private void writeStatusLog(Order order, LocalDateTime completeTime) {
        OrderStatusLog log = new OrderStatusLog();
        log.setId(SnowflakeIdUtil.nextId());
        log.setOrderId(order.getId());
        log.setFromStatus(OrderStatus.READY.getCode());
        log.setToStatus(OrderStatus.COMPLETED.getCode());
        log.setOperatorId(0L);
        log.setOperatorRole("SYSTEM");
        log.setRemark("取餐核销完成");
        log.setCreateTime(completeTime);
        orderStatusLogMapper.insert(log);
    }

    private BusinessException remoteFailure(Result<?> result, String defaultMessage) {
        String message = result == null || result.getMessage() == null || result.getMessage().isBlank()
                ? defaultMessage : result.getMessage();
        return new BusinessException(ErrorCode.INTERNAL_ERROR, message);
    }
}
