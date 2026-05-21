package cn.zhongjiating.canteen.pickup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("call_event")
public class CallEvent {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long merchantId;
    private Long orderId;
    private String pickupNo;
    private String callType;
    private LocalDateTime callTime;
}
