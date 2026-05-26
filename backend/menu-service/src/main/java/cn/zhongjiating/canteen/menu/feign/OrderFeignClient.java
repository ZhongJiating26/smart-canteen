package cn.zhongjiating.canteen.menu.feign;

import cn.zhongjiating.canteen.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Map;

@FeignClient(name = "order-service", contextId = "orderFeignClient")
public interface OrderFeignClient {

    @PostMapping("/internal/order/merchant-sales")
    Result<Map<Long, Integer>> getMerchantMonthlySales(@RequestBody List<Long> merchantIds);
}
