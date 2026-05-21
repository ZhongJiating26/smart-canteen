package cn.zhongjiating.canteen.menu.service;

import cn.zhongjiating.canteen.menu.dto.UpdateMenuConfigRequest;
import cn.zhongjiating.canteen.menu.vo.MenuConfigVO;

import java.util.List;

public interface MenuConfigService {

    List<MenuConfigVO> list();

    MenuConfigVO update(Long operatorId, UpdateMenuConfigRequest request);
}
