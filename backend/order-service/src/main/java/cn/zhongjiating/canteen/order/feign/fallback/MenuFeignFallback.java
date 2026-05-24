package cn.zhongjiating.canteen.order.feign.fallback;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.order.dto.InventoryOperationRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationResponse;
import cn.zhongjiating.canteen.order.dto.MenuSnapshotRequest;
import cn.zhongjiating.canteen.order.dto.MenuSnapshotResponse;
import cn.zhongjiating.canteen.order.dto.MerchantLookupResponse;
import cn.zhongjiating.canteen.order.feign.MenuFeignClient;
import org.springframework.stereotype.Component;

@Component
public class MenuFeignFallback implements MenuFeignClient {

    @Override
    public Result<InventoryOperationResponse> reserve(InventoryOperationRequest request) {
        return unavailableInventory();
    }

    @Override
    public Result<InventoryOperationResponse> release(InventoryOperationRequest request) {
        return unavailableInventory();
    }

    @Override
    public Result<InventoryOperationResponse> consume(InventoryOperationRequest request) {
        return unavailableInventory();
    }

    @Override
    public Result<MenuSnapshotResponse> snapshot(MenuSnapshotRequest request) {
        return Result.error(ErrorCode.INTERNAL_ERROR, "菜单服务暂不可用");
    }

    @Override
    public Result<MerchantLookupResponse> merchantByUserId(Long userId) {
        return Result.error(ErrorCode.INTERNAL_ERROR, "菜单服务暂不可用");
    }

    private Result<InventoryOperationResponse> unavailableInventory() {
        return Result.error(ErrorCode.INTERNAL_ERROR, "库存服务暂不可用");
    }
}
