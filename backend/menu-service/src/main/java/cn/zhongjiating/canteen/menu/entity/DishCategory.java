package cn.zhongjiating.canteen.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dish_category")
public class DishCategory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long merchantId;
    private String name;
    private Integer sort;
    private LocalDateTime createTime;
}
