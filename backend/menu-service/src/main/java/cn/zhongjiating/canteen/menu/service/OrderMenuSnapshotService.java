package cn.zhongjiating.canteen.menu.service;

import cn.zhongjiating.canteen.menu.dto.OrderMenuSnapshotRequest;
import cn.zhongjiating.canteen.menu.vo.OrderMenuSnapshotVO;

public interface OrderMenuSnapshotService {

    OrderMenuSnapshotVO snapshot(OrderMenuSnapshotRequest request);
}
