package cn.zhongjiating.canteen.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_menu_item")
public class DailyMenuItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long dailyMenuId;
    private Long dishId;
    private Integer dailyStock;
    private Integer totalStock;
    private Integer remainingStock;
    private Integer reservedStock;
    private Integer alertThreshold;
    private Integer isListed;
    private LocalDate lastResetDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
