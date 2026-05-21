package cn.zhongjiating.canteen.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("daily_menu")
public class DailyMenu {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long merchantId;
    private String mealPeriod;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
