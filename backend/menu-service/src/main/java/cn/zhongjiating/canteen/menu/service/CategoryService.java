package cn.zhongjiating.canteen.menu.service;

import cn.zhongjiating.canteen.menu.dto.CategoryRequest;
import cn.zhongjiating.canteen.menu.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> list(Long merchantId);

    CategoryVO create(CategoryRequest request);
}
