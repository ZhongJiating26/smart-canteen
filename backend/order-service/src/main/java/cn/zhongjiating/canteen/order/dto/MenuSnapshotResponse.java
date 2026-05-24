package cn.zhongjiating.canteen.order.dto;

import java.util.List;

public record MenuSnapshotResponse(Long merchantId, List<MenuItemSnapshotResponse> items) {
}
