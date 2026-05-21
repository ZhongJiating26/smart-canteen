package cn.zhongjiating.canteen.pickup.vo;

public record PickupPositionVO(
        Long orderId,
        String pickupNo,
        String status,
        Integer preparingPosition,
        Integer preparingAhead,
        Integer waitingPosition,
        Integer waitingAhead
) {
}
