package cn.zhongjiating.canteen.menu.mapper;

import cn.zhongjiating.canteen.menu.entity.DailyMenuItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface DailyMenuItemMapper extends BaseMapper<DailyMenuItem> {

    @Update("""
            UPDATE daily_menu_item
            SET reserved_stock = reserved_stock + #{quantity},
                remaining_stock = remaining_stock - #{quantity}
            WHERE id = #{id}
              AND remaining_stock >= #{quantity}
            """)
    int reserve(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("""
            UPDATE daily_menu_item
            SET reserved_stock = reserved_stock - #{quantity},
                remaining_stock = remaining_stock + #{quantity}
            WHERE id = #{id}
              AND reserved_stock >= #{quantity}
            """)
    int release(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("""
            UPDATE daily_menu_item
            SET reserved_stock = reserved_stock - #{quantity}
            WHERE id = #{id}
              AND reserved_stock >= #{quantity}
            """)
    int consume(@Param("id") Long id, @Param("quantity") Integer quantity);
}
