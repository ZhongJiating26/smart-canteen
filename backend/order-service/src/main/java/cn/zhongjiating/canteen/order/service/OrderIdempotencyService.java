package cn.zhongjiating.canteen.order.service;

public interface OrderIdempotencyService {

    Long findExistingOrderId(Long userId, String idempotentKey, String requestHash);

    void saveAfterCommit(Long userId, String idempotentKey, String requestHash, Long orderId);
}
