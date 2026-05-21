package cn.zhongjiating.canteen.pickup.vo;

import java.util.List;

public record QueueVO(Long merchantId, List<QueueItemVO> queue, Integer total) {
}
