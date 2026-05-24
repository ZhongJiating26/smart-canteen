package cn.zhongjiating.canteen.order.feign.fallback;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.order.dto.PickupNoResponse;
import cn.zhongjiating.canteen.order.dto.PickupPreparingRequest;
import cn.zhongjiating.canteen.order.dto.PickupQrcodeResponse;
import cn.zhongjiating.canteen.order.dto.PickupQueueRequest;
import cn.zhongjiating.canteen.order.feign.PickupFeignClient;
import org.springframework.stereotype.Component;

@Component
public class PickupFeignFallback implements PickupFeignClient {

    @Override
    public Result<PickupNoResponse> enqueuePreparing(PickupPreparingRequest request) {
        return Result.error(ErrorCode.INTERNAL_ERROR, "取餐服务暂不可用");
    }

    @Override
    public Result<PickupQrcodeResponse> enqueueWaiting(PickupQueueRequest request) {
        return Result.error(ErrorCode.INTERNAL_ERROR, "取餐服务暂不可用");
    }

    @Override
    public Result<Void> removeQueue(PickupQueueRequest request) {
        return Result.error(ErrorCode.INTERNAL_ERROR, "取餐服务暂不可用");
    }
}
