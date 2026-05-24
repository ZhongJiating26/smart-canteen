package cn.zhongjiating.canteen.pickup.feign;

import cn.zhongjiating.canteen.common.dto.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "order-service", path = "/internal/order")
public interface OrderFeignClient {

    @PostMapping("/{orderId}/complete")
    Result<Void> complete(@PathVariable("orderId") Long orderId);
}
