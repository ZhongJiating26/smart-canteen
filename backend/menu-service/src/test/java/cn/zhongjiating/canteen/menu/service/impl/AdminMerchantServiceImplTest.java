package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.menu.dto.AdminMerchantCreateRequest;
import cn.zhongjiating.canteen.menu.entity.Merchant;
import cn.zhongjiating.canteen.menu.feign.UserAccountFeignClient;
import cn.zhongjiating.canteen.menu.mapper.MerchantMapper;
import cn.zhongjiating.canteen.menu.vo.MerchantAccountVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMerchantServiceImplTest {

    @Mock
    private MerchantMapper merchantMapper;
    @Mock
    private UserAccountFeignClient userAccountFeignClient;

    @Test
    void createUsesMerchantAccountAndStoresWindow() {
        AdminMerchantServiceImpl service = new AdminMerchantServiceImpl(merchantMapper, userAccountFeignClient);
        when(userAccountFeignClient.createMerchantAccount(any()))
                .thenReturn(Result.success(new MerchantAccountVO(1000004L, "13800138004", "MERCHANT", 1)));

        service.create(new AdminMerchantCreateRequest("13800138004", "123456", " 二号窗口 ",
                " 李店长 ", "13800138004", " 面食 "));

        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
        verify(merchantMapper).insert(merchantCaptor.capture());
        Merchant merchant = merchantCaptor.getValue();
        assertThat(merchant.getUserId()).isEqualTo(1000004L);
        assertThat(merchant.getName()).isEqualTo("二号窗口");
        assertThat(merchant.getContactName()).isEqualTo("李店长");
        assertThat(merchant.getCategory()).isEqualTo("面食");
        assertThat(merchant.getStatus()).isEqualTo(1);
    }
}
