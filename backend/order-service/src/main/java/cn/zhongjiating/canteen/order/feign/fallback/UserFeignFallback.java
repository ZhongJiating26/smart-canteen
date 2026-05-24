package cn.zhongjiating.canteen.order.feign.fallback;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.order.dto.PayPasswordVerifyRequest;
import cn.zhongjiating.canteen.order.dto.PayPasswordVerifyResponse;
import cn.zhongjiating.canteen.order.feign.UserFeignClient;
import org.springframework.stereotype.Component;

@Component
public class UserFeignFallback implements UserFeignClient {

    @Override
    public Result<PayPasswordVerifyResponse> verifyPayPassword(PayPasswordVerifyRequest request) {
        return Result.error(ErrorCode.INTERNAL_ERROR, "支付服务暂不可用");
    }
}
