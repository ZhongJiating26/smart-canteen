package cn.zhongjiating.canteen.menu.vo;

import java.util.List;

public record OrderMenuSnapshotVO(Long merchantId, List<OrderMenuItemSnapshotVO> items) {
}
