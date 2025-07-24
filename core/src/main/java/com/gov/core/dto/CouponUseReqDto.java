package com.gov.core.dto;

import java.math.BigDecimal;

public record CouponUseReqDto(
    String couponId,
    String userId,
    BigDecimal amount,
    String merchantId
) {

}
