package cn.zhongjiating.canteen.pickup.mapper;

import cn.zhongjiating.canteen.pickup.entity.PreparingQueueItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PreparingQueueItemMapper extends BaseMapper<PreparingQueueItem> {

    @Select("SELECT COALESCE(MAX(position), 0) FROM preparing_queue_item WHERE merchant_id = #{merchantId}")
    int selectMaxPosition(@Param("merchantId") Long merchantId);
}
