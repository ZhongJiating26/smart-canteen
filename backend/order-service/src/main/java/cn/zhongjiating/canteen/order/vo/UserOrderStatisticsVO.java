package cn.zhongjiating.canteen.order.vo;

import java.math.BigDecimal;

public record UserOrderStatisticsVO(
        Long orderCount,
        BigDecimal totalSpend
) {
}
