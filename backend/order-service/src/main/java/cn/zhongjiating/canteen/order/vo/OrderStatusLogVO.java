package cn.zhongjiating.canteen.order.vo;

import cn.zhongjiating.canteen.order.entity.OrderStatusLog;

import java.time.LocalDateTime;

public record OrderStatusLogVO(
        String fromStatus,
        String toStatus,
        Long operatorId,
        String operatorRole,
        String remark,
        LocalDateTime createTime
) {
    public static OrderStatusLogVO from(OrderStatusLog log) {
        return new OrderStatusLogVO(log.getFromStatus(), log.getToStatus(), log.getOperatorId(),
                log.getOperatorRole(), log.getRemark(), log.getCreateTime());
    }
}
