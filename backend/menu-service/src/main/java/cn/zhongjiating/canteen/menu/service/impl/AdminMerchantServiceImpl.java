package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.menu.dto.AdminMerchantCreateRequest;
import cn.zhongjiating.canteen.menu.dto.AdminMerchantUpdateRequest;
import cn.zhongjiating.canteen.menu.dto.CreateMerchantAccountRequest;
import cn.zhongjiating.canteen.menu.entity.Merchant;
import cn.zhongjiating.canteen.menu.feign.UserAccountFeignClient;
import cn.zhongjiating.canteen.menu.mapper.MerchantMapper;
import cn.zhongjiating.canteen.menu.service.AdminMerchantService;
import cn.zhongjiating.canteen.menu.vo.MerchantAccountVO;
import cn.zhongjiating.canteen.menu.vo.MerchantVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMerchantServiceImpl implements AdminMerchantService {

    private static final long MAX_PAGE_SIZE = 100L;

    private final MerchantMapper merchantMapper;
    private final UserAccountFeignClient userAccountFeignClient;

    public AdminMerchantServiceImpl(MerchantMapper merchantMapper, UserAccountFeignClient userAccountFeignClient) {
        this.merchantMapper = merchantMapper;
        this.userAccountFeignClient = userAccountFeignClient;
    }

    @Override
    public PageResult<MerchantVO> page(long page, long size, String keyword, Integer status) {
        long current = Math.max(page, 1L);
        long pageSize = Math.min(Math.max(size, 1L), MAX_PAGE_SIZE);
        LambdaQueryWrapper<Merchant> query = new LambdaQueryWrapper<Merchant>()
                .eq(status != null, Merchant::getStatus, status)
                .like(keyword != null && !keyword.isBlank(), Merchant::getName,
                        keyword == null ? null : keyword.trim())
                .orderByDesc(Merchant::getCreateTime);
        Page<Merchant> merchantPage = merchantMapper.selectPage(Page.of(current, pageSize), query);
        return PageResult.of(merchantPage.getRecords().stream().map(MerchantVO::from).toList(),
                merchantPage.getTotal(), merchantPage.getSize(), merchantPage.getCurrent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantVO create(AdminMerchantCreateRequest request) {
        Result<MerchantAccountVO> accountResult = userAccountFeignClient.createMerchantAccount(
                new CreateMerchantAccountRequest(request.phone().trim(), request.password(), request.name().trim()));
        MerchantAccountVO account = accountResult == null ? null : accountResult.getData();
        if (account == null || account.userId() == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "商家账号创建失败");
        }
        Merchant merchant = new Merchant();
        merchant.setUserId(account.userId());
        apply(request.name(), request.contactName(), request.contactPhone(), request.category(), merchant);
        merchant.setStatus(1);
        merchantMapper.insert(merchant);
        return MerchantVO.from(merchant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantVO update(Long merchantId, AdminMerchantUpdateRequest request) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商家窗口不存在");
        }
        apply(request.name(), request.contactName(), request.contactPhone(), request.category(), merchant);
        merchant.setStatus(request.status());
        merchantMapper.updateById(merchant);
        return MerchantVO.from(merchant);
    }

    private void apply(String name, String contactName, String contactPhone, String category, Merchant merchant) {
        merchant.setName(name.trim());
        merchant.setContactName(normalizeBlank(contactName));
        merchant.setContactPhone(normalizeBlank(contactPhone));
        merchant.setCategory(normalizeBlank(category));
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
