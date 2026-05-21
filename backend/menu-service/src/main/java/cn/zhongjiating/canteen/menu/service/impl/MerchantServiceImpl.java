package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.context.UserContext;
import cn.zhongjiating.canteen.common.context.UserContextHolder;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.common.exception.ForbiddenException;
import cn.zhongjiating.canteen.menu.entity.Merchant;
import cn.zhongjiating.canteen.menu.mapper.MerchantMapper;
import cn.zhongjiating.canteen.menu.service.MerchantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;

    public MerchantServiceImpl(MerchantMapper merchantMapper) {
        this.merchantMapper = merchantMapper;
    }

    @Override
    public Merchant requireCurrentMerchant() {
        UserContext context = UserContextHolder.require();
        if (!context.isMerchant()) {
            throw new ForbiddenException("仅商家可操作菜单");
        }
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, context.getUserId())
                .eq(Merchant::getStatus, 1));
        if (merchant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商家信息不存在或已停用");
        }
        return merchant;
    }

    @Override
    public Merchant requireMerchant(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商家信息不存在或已停用");
        }
        return merchant;
    }
}
