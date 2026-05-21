package cn.zhongjiating.canteen.menu.service;

import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.menu.dto.AdminMerchantCreateRequest;
import cn.zhongjiating.canteen.menu.dto.AdminMerchantUpdateRequest;
import cn.zhongjiating.canteen.menu.vo.MerchantVO;

public interface AdminMerchantService {

    PageResult<MerchantVO> page(long page, long size, String keyword, Integer status);

    MerchantVO create(AdminMerchantCreateRequest request);

    MerchantVO update(Long merchantId, AdminMerchantUpdateRequest request);
}
