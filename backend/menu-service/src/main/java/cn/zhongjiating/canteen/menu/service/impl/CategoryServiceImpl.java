package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.menu.dto.CategoryRequest;
import cn.zhongjiating.canteen.menu.entity.DishCategory;
import cn.zhongjiating.canteen.menu.entity.Merchant;
import cn.zhongjiating.canteen.menu.mapper.DishCategoryMapper;
import cn.zhongjiating.canteen.menu.service.CategoryService;
import cn.zhongjiating.canteen.menu.service.MerchantService;
import cn.zhongjiating.canteen.menu.vo.CategoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final DishCategoryMapper categoryMapper;
    private final MerchantService merchantService;

    public CategoryServiceImpl(DishCategoryMapper categoryMapper, MerchantService merchantService) {
        this.categoryMapper = categoryMapper;
        this.merchantService = merchantService;
    }

    @Override
    public List<CategoryVO> list(Long merchantId) {
        LambdaQueryWrapper<DishCategory> query = new LambdaQueryWrapper<DishCategory>()
                .orderByAsc(DishCategory::getSort)
                .orderByAsc(DishCategory::getId);
        if (merchantId != null) {
            merchantService.requireMerchant(merchantId);
            query.eq(DishCategory::getMerchantId, merchantId);
        }
        return categoryMapper.selectList(query).stream().map(CategoryVO::from).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO create(CategoryRequest request) {
        Merchant merchant = merchantService.requireCurrentMerchant();
        DishCategory category = new DishCategory();
        category.setMerchantId(merchant.getId());
        category.setName(request.name().trim());
        category.setSort(request.sort());
        categoryMapper.insert(category);
        return CategoryVO.from(category);
    }
}
