package cn.zhongjiating.canteen.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory_log")
public class InventoryLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long dailyMenuItemId;
    private String changeType;
    private Integer changeAmount;
    private Integer beforeRemaining;
    private Integer afterRemaining;
    private Long orderId;
    private LocalDateTime createTime;
}
