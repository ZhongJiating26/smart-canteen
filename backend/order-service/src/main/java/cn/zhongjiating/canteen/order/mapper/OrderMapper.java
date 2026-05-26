package cn.zhongjiating.canteen.order.mapper;

import cn.zhongjiating.canteen.order.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface OrderMapper extends BaseMapper<Order> {

    @Update("""
            UPDATE `order`
            SET status = 'PAID',
                pay_time = #{payTime},
                pickup_no = #{pickupNo},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND status = 'PENDING_PAY'
            """)
    int markPaid(@Param("orderId") Long orderId, @Param("payTime") LocalDateTime payTime, @Param("pickupNo") String pickupNo);

    @Update("""
            UPDATE `order`
            SET status = 'CANCELLED',
                cancel_reason = #{cancelReason},
                cancel_time = #{cancelTime},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND status = #{fromStatus}
            """)
    int cancelIfStatus(@Param("orderId") Long orderId, @Param("fromStatus") String fromStatus,
                       @Param("cancelReason") String cancelReason, @Param("cancelTime") LocalDateTime cancelTime);

    @Update("""
            UPDATE `order`
            SET status = 'PREPARING',
                accept_time = #{acceptTime},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND status = 'PAID'
            """)
    int markPreparing(@Param("orderId") Long orderId, @Param("acceptTime") LocalDateTime acceptTime);

    @Update("""
            UPDATE `order`
            SET status = 'READY',
                pickup_qrcode_id = #{pickupQrcodeId},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND status = 'PREPARING'
            """)
    int markReady(@Param("orderId") Long orderId, @Param("pickupQrcodeId") Long pickupQrcodeId);

    @Update("""
            UPDATE `order`
            SET status = 'COMPLETED',
                complete_time = #{completeTime},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId}
              AND status = 'READY'
            """)
    int markCompleted(@Param("orderId") Long orderId, @Param("completeTime") LocalDateTime completeTime);
}
