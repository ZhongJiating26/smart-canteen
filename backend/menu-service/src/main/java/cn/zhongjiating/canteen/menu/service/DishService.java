package cn.zhongjiating.canteen.menu.service;

import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.menu.dto.DishRequest;
import cn.zhongjiating.canteen.menu.entity.Dish;
import cn.zhongjiating.canteen.menu.vo.DishVO;
import org.springframework.web.multipart.MultipartFile;

public interface DishService {

    PageResult<DishVO> page(long page, long size, Long categoryId, String name, Integer status);

    DishVO get(Long dishId);

    Dish requireDish(Long dishId);

    DishVO create(DishRequest request);

    DishVO update(Long dishId, DishRequest request);

    void disable(Long dishId);

    DishVO updateImage(Long dishId, MultipartFile image);
}
