package cn.zhongjiating.canteen.pickup.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MerchantLookupResponse(Long merchantId, Long userId) {
}
