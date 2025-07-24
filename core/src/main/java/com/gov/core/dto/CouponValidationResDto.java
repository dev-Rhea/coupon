package com.gov.core.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record CouponValidationResDto(
    boolean isValid,
    String message,
    BigDecimal availableAmount
) {

    public static CouponValidationResDto success(BigDecimal availableAmount) {
        return CouponValidationResDto.builder()
            .isValid(true)
            .message("사용 가능한 쿠폰입니다.")
            .availableAmount(availableAmount)
            .build();
    }

    public static CouponValidationResDto failure(String message) {
        return CouponValidationResDto.builder()
            .isValid(false)
            .message(message)
            .availableAmount(BigDecimal.ZERO)
            .build();
    }

}
