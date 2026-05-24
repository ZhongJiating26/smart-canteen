package cn.zhongjiating.canteen.order.feign;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.order.dto.PayPasswordVerifyRequest;
import cn.zhongjiating.canteen.order.dto.PayPasswordVerifyResponse;
import cn.zhongjiating.canteen.order.feign.fallback.UserFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", contextId = "orderUserFeignClient", fallback = UserFeignFallback.class)
public interface UserFeignClient {

    @PostMapping("/api/user/verify-pay-password")
    Result<PayPasswordVerifyResponse> verifyPayPassword(@RequestBody PayPasswordVerifyRequest request);
}
