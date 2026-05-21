package cn.zhongjiating.canteen.menu.feign;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.menu.dto.CreateMerchantAccountRequest;
import cn.zhongjiating.canteen.menu.vo.MerchantAccountVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", contextId = "userAccountFeignClient")
public interface UserAccountFeignClient {

    @PostMapping("/internal/user/merchant-accounts")
    Result<MerchantAccountVO> createMerchantAccount(@RequestBody CreateMerchantAccountRequest request);
}
