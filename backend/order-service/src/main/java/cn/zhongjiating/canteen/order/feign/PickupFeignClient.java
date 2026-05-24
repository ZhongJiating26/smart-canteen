package cn.zhongjiating.canteen.order.feign;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.order.dto.PickupNoResponse;
import cn.zhongjiating.canteen.order.dto.PickupPreparingRequest;
import cn.zhongjiating.canteen.order.dto.PickupQrcodeResponse;
import cn.zhongjiating.canteen.order.dto.PickupQueueRequest;
import cn.zhongjiating.canteen.order.feign.fallback.PickupFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pickup-service", contextId = "orderPickupFeignClient", path = "/api/pickup/internal",
        fallback = PickupFeignFallback.class)
public interface PickupFeignClient {

    @PostMapping("/enqueue-preparing")
    Result<PickupNoResponse> enqueuePreparing(@RequestBody PickupPreparingRequest request);

    @PostMapping("/enqueue-waiting")
    Result<PickupQrcodeResponse> enqueueWaiting(@RequestBody PickupQueueRequest request);

    @PostMapping("/remove-queue")
    Result<Void> removeQueue(@RequestBody PickupQueueRequest request);
}
