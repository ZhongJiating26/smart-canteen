package cn.zhongjiating.canteen.pickup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pickup_qrcode")
public class PickupQrcode {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private String qrcodeContent;
    private String pickupCode;
    private Integer isVerified;
    private LocalDateTime verifyTime;
    private LocalDateTime createTime;
}
