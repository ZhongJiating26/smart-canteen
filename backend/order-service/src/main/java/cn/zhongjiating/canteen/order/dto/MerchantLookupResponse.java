package cn.zhongjiating.canteen.order.dto;

public record MerchantLookupResponse(Long merchantId, Long userId, String name, Integer status) {
}
