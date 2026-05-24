package cn.zhongjiating.canteen.order.feign;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.order.dto.InventoryOperationRequest;
import cn.zhongjiating.canteen.order.dto.InventoryOperationResponse;
import cn.zhongjiating.canteen.order.dto.MenuSnapshotRequest;
import cn.zhongjiating.canteen.order.dto.MenuSnapshotResponse;
import cn.zhongjiating.canteen.order.dto.MerchantLookupResponse;
import cn.zhongjiating.canteen.order.feign.fallback.MenuFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "menu-service", contextId = "orderMenuFeignClient", fallback = MenuFeignFallback.class)
public interface MenuFeignClient {

    @PostMapping("/api/menu/inventory/reserve")
    Result<InventoryOperationResponse> reserve(@RequestBody InventoryOperationRequest request);

    @PostMapping("/api/menu/inventory/release")
    Result<InventoryOperationResponse> release(@RequestBody InventoryOperationRequest request);

    @PostMapping("/api/menu/inventory/consume")
    Result<InventoryOperationResponse> consume(@RequestBody InventoryOperationRequest request);

    @PostMapping("/internal/menu/order/snapshot")
    Result<MenuSnapshotResponse> snapshot(@RequestBody MenuSnapshotRequest request);

    @GetMapping("/internal/menu/merchants/by-user/{userId}")
    Result<MerchantLookupResponse> merchantByUserId(@PathVariable("userId") Long userId);
}
