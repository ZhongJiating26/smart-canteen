package cn.zhongjiating.canteen.pickup.service;

import cn.zhongjiating.canteen.pickup.dto.CallPickupRequest;
import cn.zhongjiating.canteen.pickup.dto.EnqueuePreparingRequest;
import cn.zhongjiating.canteen.pickup.dto.QueueOrderRequest;
import cn.zhongjiating.canteen.pickup.vo.PickupNoVO;
import cn.zhongjiating.canteen.pickup.vo.PickupPositionVO;
import cn.zhongjiating.canteen.pickup.vo.QrcodeVO;
import cn.zhongjiating.canteen.pickup.vo.QueueVO;
import cn.zhongjiating.canteen.pickup.vo.SnapshotVO;

public interface PickupQueueService {

    PickupNoVO enqueuePreparing(EnqueuePreparingRequest request);

    QrcodeVO enqueueWaiting(QueueOrderRequest request);

    void removeQueue(QueueOrderRequest request);

    QueueVO preparing(Long merchantId);

    QueueVO waiting(Long merchantId);

    SnapshotVO snapshot(Long merchantId);

    PickupPositionVO position(Long orderId);

    void call(CallPickupRequest request);

    QrcodeVO qrcode(Long orderId);
}
