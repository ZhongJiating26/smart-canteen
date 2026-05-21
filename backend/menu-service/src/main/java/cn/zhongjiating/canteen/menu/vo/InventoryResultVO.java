package cn.zhongjiating.canteen.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "库存内部接口处理结果")
public record InventoryResultVO(boolean success) {

    public static InventoryResultVO ok() {
        return new InventoryResultVO(true);
    }
}
