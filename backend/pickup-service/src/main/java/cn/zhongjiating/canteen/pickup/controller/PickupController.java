package cn.zhongjiating.canteen.pickup.controller;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.pickup.dto.CallPickupRequest;
import cn.zhongjiating.canteen.pickup.dto.MerchantLookupResponse;
import cn.zhongjiating.canteen.pickup.dto.VerifyPickupRequest;
import cn.zhongjiating.canteen.pickup.feign.MenuFeignClient;
import cn.zhongjiating.canteen.pickup.service.PickupQueueService;
import cn.zhongjiating.canteen.pickup.service.VerifyPickupService;
import cn.zhongjiating.canteen.pickup.vo.QrcodeVO;
import cn.zhongjiating.canteen.pickup.vo.PickupPositionVO;
import cn.zhongjiating.canteen.pickup.vo.QueueVO;
import cn.zhongjiating.canteen.pickup.vo.SnapshotVO;
import cn.zhongjiating.canteen.pickup.vo.VerifyPickupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "取餐排队", description = "队列展示、叫号和核销")
@RestController
@RequestMapping("/api/pickup")
public class PickupController {

    private final PickupQueueService pickupQueueService;
    private final VerifyPickupService verifyPickupService;
    private final MenuFeignClient menuFeignClient;

    public PickupController(PickupQueueService pickupQueueService, VerifyPickupService verifyPickupService,
                            MenuFeignClient menuFeignClient) {
        this.pickupQueueService = pickupQueueService;
        this.verifyPickupService = verifyPickupService;
        this.menuFeignClient = menuFeignClient;
    }

    @Operation(summary = "查询制作中队列")
    @GetMapping("/preparing/{merchantId}")
    public Result<QueueVO> preparing(@PathVariable Long merchantId) {
        return Result.success(pickupQueueService.preparing(merchantId));
    }

    @Operation(summary = "查询待取餐队列")
    @GetMapping("/waiting/{merchantId}")
    public Result<QueueVO> waiting(@PathVariable Long merchantId) {
        return Result.success(pickupQueueService.waiting(merchantId));
    }

    @Operation(summary = "查询大屏快照")
    @GetMapping("/snapshot/{merchantId}")
    public Result<SnapshotVO> snapshot(@PathVariable Long merchantId) {
        return Result.success(pickupQueueService.snapshot(merchantId));
    }

    @Operation(summary = "查询取餐二维码")
    @GetMapping("/qrcode/{orderId}")
    public Result<QrcodeVO> qrcode(@PathVariable Long orderId) {
        UserContext context = UserContextHolder.require();
        if (!context.isUser()) {
            throw new ForbiddenException();
        }
        return Result.success(pickupQueueService.qrcode(orderId));
    }

    @Operation(summary = "查询我的排队位置")
    @GetMapping("/my-position/{orderId}")
    public Result<PickupPositionVO> position(@PathVariable Long orderId) {
        UserContext context = UserContextHolder.require();
        if (!context.isUser()) {
            throw new ForbiddenException();
        }
        return Result.success(pickupQueueService.position(orderId));
    }

    @Operation(summary = "商家手动叫号")
    @PostMapping("/call")
    public Result<Void> call(@Valid @RequestBody CallPickupRequest request) {
        requireMerchant(request.merchantId());
        pickupQueueService.call(request);
        return Result.success();
    }

    @Operation(summary = "核销取餐")
    @PostMapping("/verify")
    public Result<VerifyPickupVO> verify(@Valid @RequestBody VerifyPickupRequest request) {
        UserContext context = requireMerchant(request.merchantId());
        return Result.success(verifyPickupService.verify(context.getUserId(), request));
    }

    private UserContext requireMerchant(Long merchantId) {
        UserContext context = UserContextHolder.require();
        if (!context.isMerchant()) {
            throw new ForbiddenException();
        }
        Result<MerchantLookupResponse> result = menuFeignClient.merchantByUserId(context.getUserId());
        if (result == null || result.getCode() == null || result.getCode() != 200 || result.getData() == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "商家窗口身份查询失败");
        }
        if (!merchantId.equals(result.getData().merchantId())) {
            throw new ForbiddenException("无权操作该商家窗口");
        }
        return context;
    }
}
