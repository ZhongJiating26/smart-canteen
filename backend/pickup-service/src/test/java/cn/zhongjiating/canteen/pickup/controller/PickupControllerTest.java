package cn.zhongjiating.canteen.pickup.controller;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.RoleEnum;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.pickup.dto.CallPickupRequest;
import cn.zhongjiating.canteen.pickup.dto.MerchantLookupResponse;
import cn.zhongjiating.canteen.pickup.feign.MenuFeignClient;
import cn.zhongjiating.canteen.pickup.service.PickupQueueService;
import cn.zhongjiating.canteen.pickup.service.VerifyPickupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PickupControllerTest {

    @Mock
    private PickupQueueService pickupQueueService;
    @Mock
    private VerifyPickupService verifyPickupService;
    @Mock
    private MenuFeignClient menuFeignClient;

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void merchantCanOperateOwnedWindowWhenUserAndMerchantIdsDiffer() {
        UserContextHolder.set(UserContext.of(1000002L, "merchant", RoleEnum.MERCHANT));
        when(menuFeignClient.merchantByUserId(1000002L))
                .thenReturn(Result.success(new MerchantLookupResponse(2000001L, 1000002L)));
        PickupController controller = controller();
        CallPickupRequest request = new CallPickupRequest(2000001L, 8000001L);

        controller.call(request);

        verify(pickupQueueService).call(request);
    }

    @Test
    void merchantCannotOperateAnotherWindow() {
        UserContextHolder.set(UserContext.of(1000002L, "merchant", RoleEnum.MERCHANT));
        when(menuFeignClient.merchantByUserId(1000002L))
                .thenReturn(Result.success(new MerchantLookupResponse(2000001L, 1000002L)));
        PickupController controller = controller();

        assertThatThrownBy(() -> controller.call(new CallPickupRequest(2000002L, 8000001L)))
                .isInstanceOf(ForbiddenException.class);
    }

    private PickupController controller() {
        return new PickupController(pickupQueueService, verifyPickupService, menuFeignClient);
    }
}
