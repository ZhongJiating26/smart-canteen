package cn.zhongjiating.canteen.menu.service;

import cn.zhongjiating.canteen.menu.entity.Merchant;

public interface MerchantService {

    Merchant requireCurrentMerchant();

    Merchant requireMerchant(Long merchantId);
}
