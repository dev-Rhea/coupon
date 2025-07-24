package com.gov.core.dto;

import com.gov.core.entity.Coupon;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CouponResDto(
    String couponId,
    String userId,
    String userName,
    BigDecimal originalAmount,
    BigDecimal remainingAmount,
    LocalDate expiryDate,
    String status,
    String statusDescription,
    LocalDateTime createdAt,
    boolean isExpired,
    boolean canUse
) {
    public static class Response {
        public static CouponResDto from(Coupon coupon) {
            return CouponResDto.builder()
                .couponId(coupon.getCouponId())
                .userId(coupon.getUser().getUserId())
                .userName(coupon.getUser().getName())
                .originalAmount(coupon.getOriginalAmount())
                .remainingAmount(coupon.getRemainingAmount())
                .expiryDate(coupon.getExpiryDate())
                .status(coupon.getStatus().name())
                .statusDescription(coupon.getStatus().getDescription())
                .createdAt(coupon.getCreatedAt())
                .isExpired(coupon.isExpired())
                .canUse(coupon.canUse(BigDecimal.ZERO)) // 기본값으로 0을 사용
                .build();
        }
    }

}
