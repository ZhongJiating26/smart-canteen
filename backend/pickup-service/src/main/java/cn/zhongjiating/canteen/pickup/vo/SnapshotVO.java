package cn.zhongjiating.canteen.pickup.vo;

import java.util.List;

public record SnapshotVO(
        Long merchantId,
        List<QueueItemVO> preparingQueue,
        List<QueueItemVO> waitingQueue,
        List<CallEventVO> recentCalls
) {
}
