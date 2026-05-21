package cn.zhongjiating.canteen.menu.controller;

import cn.zhongjiating.canteen.common.dto.PageResult;
import cn.zhongjiating.canteen.common.dto.Result;
import cn.zhongjiating.canteen.menu.dto.DishRequest;
import cn.zhongjiating.canteen.menu.entity.Dish;
import cn.zhongjiating.canteen.menu.service.DishService;
import cn.zhongjiating.canteen.menu.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "菜品管理", description = "商家菜品 CRUD 与菜品图片")
@RestController
@RequestMapping("/api/menu")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @Operation(summary = "分页查询当前商家菜品")
    @GetMapping("/dishes")
    public Result<PageResult<DishVO>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        return Result.success(dishService.page(page, size, categoryId, name, status));
    }

    @Operation(summary = "查询菜品详情", description = "公开接口，不返回图片二进制")
    @GetMapping("/dishes/{dishId}")
    public Result<DishVO> get(
            @Parameter(description = "菜品ID", example = "5000001")
            @PathVariable Long dishId) {
        return Result.success(dishService.get(dishId));
    }

    @Operation(summary = "新增菜品")
    @PostMapping("/dishes")
    public Result<DishVO> create(@Valid @RequestBody DishRequest request) {
        return Result.success(dishService.create(request));
    }

    @Operation(summary = "修改菜品")
    @PutMapping("/dishes/{dishId}")
    public Result<DishVO> update(@PathVariable Long dishId, @Valid @RequestBody DishRequest request) {
        return Result.success(dishService.update(dishId, request));
    }

    @Operation(summary = "停用菜品", description = "逻辑删除，历史订单快照不受影响")
    @DeleteMapping("/dishes/{dishId}")
    public Result<Void> disable(@PathVariable Long dishId) {
        dishService.disable(dishId);
        return Result.success();
    }

    @Operation(summary = "更新菜品图片", description = "支持 jpg、jpeg、png、webp，单文件最大5MB")
    @PutMapping(value = "/dishes/{dishId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<DishVO> updateImage(@PathVariable Long dishId, @RequestParam("file") MultipartFile file) {
        return Result.success(dishService.updateImage(dishId, file));
    }

    @Operation(summary = "获取菜品图片", description = "公开接口，直接返回图片二进制")
    @GetMapping("/files/{dishId}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long dishId) {
        Dish dish = dishService.requireDish(dishId);
        if (dish.getImage() == null || dish.getImage().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(dish.getImage());
    }
}
