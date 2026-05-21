package cn.zhongjiating.canteen.menu.service.impl;

import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.common.enums.ErrorCode;
import cn.zhongjiating.canteen.common.exception.BusinessException;
import cn.zhongjiating.canteen.menu.dto.DishRequest;
import cn.zhongjiating.canteen.menu.entity.Dish;
import cn.zhongjiating.canteen.menu.entity.DishCategory;
import cn.zhongjiating.canteen.menu.entity.Merchant;
import cn.zhongjiating.canteen.menu.mapper.DishCategoryMapper;
import cn.zhongjiating.canteen.menu.mapper.DishMapper;
import cn.zhongjiating.canteen.menu.service.DishService;
import cn.zhongjiating.canteen.menu.service.MerchantService;
import cn.zhongjiating.canteen.menu.vo.DishVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Service
public class DishServiceImpl implements DishService {

    private static final long MAX_PAGE_SIZE = 100L;
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final DishMapper dishMapper;
    private final DishCategoryMapper categoryMapper;
    private final MerchantService merchantService;

    public DishServiceImpl(DishMapper dishMapper, DishCategoryMapper categoryMapper, MerchantService merchantService) {
        this.dishMapper = dishMapper;
        this.categoryMapper = categoryMapper;
        this.merchantService = merchantService;
    }

    @Override
    public PageResult<DishVO> page(long page, long size, Long categoryId, String name, Integer status) {
        Merchant merchant = merchantService.requireCurrentMerchant();
        long current = Math.max(page, 1L);
        long pageSize = Math.min(Math.max(size, 1L), MAX_PAGE_SIZE);
        LambdaQueryWrapper<Dish> query = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getMerchantId, merchant.getId())
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .eq(status != null, Dish::getStatus, status)
                .like(name != null && !name.isBlank(), Dish::getName, name == null ? null : name.trim())
                .orderByDesc(Dish::getCreateTime);
        Page<Dish> dishPage = dishMapper.selectPage(Page.of(current, pageSize), query);
        return PageResult.of(dishPage.getRecords().stream().map(DishVO::from).toList(),
                dishPage.getTotal(), dishPage.getSize(), dishPage.getCurrent());
    }

    @Override
    public DishVO get(Long dishId) {
        return DishVO.from(requireDish(dishId));
    }

    @Override
    public Dish requireDish(Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "菜品不存在");
        }
        return dish;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DishVO create(DishRequest request) {
        Merchant merchant = merchantService.requireCurrentMerchant();
        requireCategory(merchant.getId(), request.categoryId());
        Dish dish = new Dish();
        apply(request, dish);
        dish.setMerchantId(merchant.getId());
        dish.setStatus(1);
        dishMapper.insert(dish);
        return DishVO.from(dish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DishVO update(Long dishId, DishRequest request) {
        Merchant merchant = merchantService.requireCurrentMerchant();
        Dish dish = requireOwnedDish(merchant.getId(), dishId);
        requireCategory(merchant.getId(), request.categoryId());
        apply(request, dish);
        dishMapper.updateById(dish);
        return DishVO.from(dish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long dishId) {
        Merchant merchant = merchantService.requireCurrentMerchant();
        Dish dish = requireOwnedDish(merchant.getId(), dishId);
        dish.setStatus(0);
        dishMapper.updateById(dish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DishVO updateImage(Long dishId, MultipartFile image) {
        Merchant merchant = merchantService.requireCurrentMerchant();
        Dish dish = requireOwnedDish(merchant.getId(), dishId);
        validateImage(image);
        try {
            dish.setImage(image.getBytes());
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "菜品图片读取失败");
        }
        dishMapper.updateById(dish);
        return DishVO.from(dish);
    }

    private void apply(DishRequest request, Dish dish) {
        dish.setCategoryId(request.categoryId());
        dish.setName(request.name().trim());
        dish.setPrice(request.price());
        dish.setDescription(request.description() == null ? null : request.description().trim());
    }

    private Dish requireOwnedDish(Long merchantId, Long dishId) {
        Dish dish = requireDish(dishId);
        if (!merchantId.equals(dish.getMerchantId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return dish;
    }

    private void requireCategory(Long merchantId, Long categoryId) {
        DishCategory category = categoryMapper.selectById(categoryId);
        if (category == null || !merchantId.equals(category.getMerchantId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "菜品分类不存在");
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "菜品图片不能为空");
        }
        if (image.getSize() > MAX_IMAGE_BYTES) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "菜品图片不能超过5MB");
        }
        String contentType = image.getContentType();
        if (contentType == null || !IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持 jpg、jpeg、png、webp 图片");
        }
    }
}
