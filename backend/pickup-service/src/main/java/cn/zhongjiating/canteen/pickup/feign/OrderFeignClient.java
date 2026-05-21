package cn.zhongjiating.canteen.pickup.feign;

import cn.zhongjiating.canteen.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", path = "/api/order/internal")
public interface OrderFeignClient {

    @PutMapping("/{orderId}/status")
    Result<Void> complete(@PathVariable("orderId") Long orderId);
}
