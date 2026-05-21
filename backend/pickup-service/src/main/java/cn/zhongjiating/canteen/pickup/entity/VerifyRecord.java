package cn.zhongjiating.canteen.pickup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("verify_record")
public class VerifyRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private Long qrcodeId;
    private Long merchantId;
    private String verifyType;
    private Long operatorId;
    private LocalDateTime verifyTime;
}
