package cn.zhongjiating.canteen.menu.controller;

import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.menu.dto.CategoryRequest;
import cn.zhongjiating.canteen.menu.service.CategoryService;
import cn.zhongjiating.canteen.menu.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "菜单分类", description = "菜品分类浏览与商家创建")
@RestController
@RequestMapping("/api/menu/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "查询菜品分类", description = "游客可按商家查询分类")
    @GetMapping
    public Result<List<CategoryVO>> list(
            @Parameter(description = "商家ID", example = "2000001")
            @RequestParam(required = false) Long merchantId) {
        return Result.success(categoryService.list(merchantId));
    }

    @Operation(summary = "新增菜品分类", description = "根据网关注入的商家用户上下文归属到当前商家")
    @PostMapping
    public Result<CategoryVO> create(@Valid @RequestBody CategoryRequest request) {
        return Result.success(categoryService.create(request));
    }
}
