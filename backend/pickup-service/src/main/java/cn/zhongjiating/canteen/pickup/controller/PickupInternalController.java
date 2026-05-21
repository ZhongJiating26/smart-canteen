package cn.zhongjiating.canteen.pickup.controller;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.pickup.dto.EnqueuePreparingRequest;
import cn.zhongjiating.canteen.pickup.dto.QueueOrderRequest;
import cn.zhongjiating.canteen.pickup.service.PickupQueueService;
import cn.zhongjiating.canteen.pickup.vo.PickupNoVO;
import cn.zhongjiating.canteen.pickup.vo.QrcodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "取餐内部队列", description = "order-service 调用的队列流转接口")
@RestController
@RequestMapping("/api/pickup/internal")
public class PickupInternalController {

    private final PickupQueueService pickupQueueService;

    public PickupInternalController(PickupQueueService pickupQueueService) {
        this.pickupQueueService = pickupQueueService;
    }

    @Operation(summary = "加入制作中队列")
    @PostMapping("/enqueue-preparing")
    public Result<PickupNoVO> enqueuePreparing(@Valid @RequestBody EnqueuePreparingRequest request) {
        return Result.success(pickupQueueService.enqueuePreparing(request));
    }

    @Operation(summary = "加入待取餐队列")
    @PostMapping("/enqueue-waiting")
    public Result<QrcodeVO> enqueueWaiting(@Valid @RequestBody QueueOrderRequest request) {
        return Result.success(pickupQueueService.enqueueWaiting(request));
    }

    @Operation(summary = "从取餐队列移除订单")
    @PostMapping("/remove-queue")
    public Result<Void> removeQueue(@Valid @RequestBody QueueOrderRequest request) {
        pickupQueueService.removeQueue(request);
        return Result.success();
    }
}
