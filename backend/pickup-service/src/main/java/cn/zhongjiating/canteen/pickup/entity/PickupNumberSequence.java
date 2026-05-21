package cn.zhongjiating.canteen.pickup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pickup_number_sequence")
public class PickupNumberSequence {

    @TableId(type = IdType.INPUT)
    private Long id;
    private String currentLetter;
    private Integer currentNumber;
    private String resetRule;
    private LocalDateTime lastResetTime;
}
