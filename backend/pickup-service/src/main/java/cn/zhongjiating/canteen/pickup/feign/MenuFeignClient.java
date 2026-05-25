package cn.zhongjiating.canteen.pickup.feign;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.pickup.dto.MerchantLookupResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "menu-service", contextId = "pickupMenuFeignClient")
public interface MenuFeignClient {

    @GetMapping("/internal/menu/merchants/by-user/{userId}")
    Result<MerchantLookupResponse> merchantByUserId(@PathVariable("userId") Long userId);
}
