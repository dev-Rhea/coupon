package com.gov.payment.dto;

import com.gov.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PaymentSearchDto(
    String userId,
    String merchantId,
    String couponId,
    PaymentStatus status,
    BigDecimal minAmount,
    BigDecimal maxAmount,
    LocalDateTime startDate,
    LocalDateTime endDate
) {

    public static PaymentSearchDto ofUser(String userId) {
        return PaymentSearchDto.builder()
            .userId(userId)
            .build();
    }

    public static PaymentSearchDto ofMerchant(String merchantId) {
        return PaymentSearchDto.builder()
            .merchantId(merchantId)
            .build();
    }

    public static PaymentSearchDto ofCoupon(String couponId) {
        return PaymentSearchDto.builder()
            .couponId(couponId)
            .build();
    }

    public static PaymentSearchDto ofStatus(PaymentStatus status) {
        return PaymentSearchDto.builder()
            .status(status)
            .build();
    }

    public static PaymentSearchDto ofAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return PaymentSearchDto.builder()
            .minAmount(minAmount)
            .maxAmount(maxAmount)
            .build();
    }

    public static PaymentSearchDto ofDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return PaymentSearchDto.builder()
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }

    public PaymentSearchDto withUserId(String userId) {
        return PaymentSearchDto.builder()
            .userId(userId)
            .merchantId(this.merchantId)
            .couponId(this.couponId)
            .status(this.status)
            .minAmount(this.minAmount)
            .maxAmount(this.maxAmount)
            .startDate(this.startDate)
            .endDate(this.endDate)
            .build();
    }

    public PaymentSearchDto withMerchantId(String merchantId) {
        return PaymentSearchDto.builder()
            .userId(this.userId)
            .merchantId(merchantId)
            .couponId(this.couponId)
            .status(this.status)
            .minAmount(this.minAmount)
            .maxAmount(this.maxAmount)
            .startDate(this.startDate)
            .endDate(this.endDate)
            .build();
    }

    public PaymentSearchDto withCouponId(String couponId) {
        return PaymentSearchDto.builder()
            .userId(this.userId)
            .merchantId(this.merchantId)
            .couponId(couponId)
            .status(this.status)
            .minAmount(this.minAmount)
            .maxAmount(this.maxAmount)
            .startDate(this.startDate)
            .endDate(this.endDate)
            .build();
    }

    public PaymentSearchDto withStatus(PaymentStatus status) {
        return PaymentSearchDto.builder()
            .userId(this.userId)
            .merchantId(this.merchantId)
            .couponId(this.couponId)
            .status(status)
            .minAmount(this.minAmount)
            .maxAmount(this.maxAmount)
            .startDate(this.startDate)
            .endDate(this.endDate)
            .build();
    }

    public PaymentSearchDto withAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return PaymentSearchDto.builder()
            .userId(this.userId)
            .merchantId(this.merchantId)
            .couponId(this.couponId)
            .status(this.status)
            .minAmount(minAmount)
            .maxAmount(maxAmount)
            .startDate(this.startDate)
            .endDate(this.endDate)
            .build();
    }

    public PaymentSearchDto withDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return PaymentSearchDto.builder()
            .userId(this.userId)
            .merchantId(this.merchantId)
            .couponId(this.couponId)
            .status(this.status)
            .minAmount(this.minAmount)
            .maxAmount(this.maxAmount)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }

    public PaymentSearchDto withUserAndMerchant(String userId, String merchantId) {
        return PaymentSearchDto.builder()
            .userId(userId)
            .merchantId(merchantId)
            .couponId(this.couponId)
            .status(this.status)
            .minAmount(this.minAmount)
            .maxAmount(this.maxAmount)
            .startDate(this.startDate)
            .endDate(this.endDate)
            .build();
    }

    public PaymentSearchDto withStatusAndDateRange(PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        return PaymentSearchDto.builder()
            .userId(this.userId)
            .merchantId(this.merchantId)
            .couponId(this.couponId)
            .status(status)
            .minAmount(this.minAmount)
            .maxAmount(this.maxAmount)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }

    // 검증 메서드들
    public boolean hasUserIdCondition() {
        return userId != null && !userId.trim().isEmpty();
    }

    public boolean hasMerchantIdCondition() {
        return merchantId != null && !merchantId.trim().isEmpty();
    }

    public boolean hasCouponIdCondition() {
        return couponId != null && !couponId.trim().isEmpty();
    }

    public boolean hasStatusCondition() {
        return status != null;
    }

    public boolean hasAmountCondition() {
        return minAmount != null || maxAmount != null;
    }

    public boolean hasDateCondition() {
        return startDate != null || endDate != null;
    }

    public boolean hasAnyCondition() {
        return hasUserIdCondition() || hasMerchantIdCondition() || hasCouponIdCondition() ||
            hasStatusCondition() || hasAmountCondition() || hasDateCondition();
    }

    // 유효성 검증
    public boolean isValid() {
        // 최소 금액이 최대 금액보다 큰 경우
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            return false;
        }

        // 시작일이 종료일보다 늦은 경우
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return false;
        }

        // 금액이 음수인 경우
        if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        return true;
    }

    // 검증 오류 메시지
    public String getValidationError() {
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            return "최소 금액이 최대 금액보다 클 수 없습니다.";
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return "시작일이 종료일보다 늦을 수 없습니다.";
        }

        if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) < 0) {
            return "최소 금액은 0 이상이어야 합니다.";
        }

        if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) < 0) {
            return "최대 금액은 0 이상이어야 합니다.";
        }

        return null; // 검증 통과
    }

    // 검색 조건 요약
    public String getSearchSummary() {
        StringBuilder summary = new StringBuilder();

        if (hasUserIdCondition()) {
            summary.append("사용자: ").append(userId).append(" ");
        }

        if (hasMerchantIdCondition()) {
            summary.append("가맹점: ").append(merchantId).append(" ");
        }

        if (hasCouponIdCondition()) {
            summary.append("쿠폰: ").append(couponId).append(" ");
        }

        if (hasStatusCondition()) {
            summary.append("상태: ").append(status.name()).append(" ");
        }

        if (hasAmountCondition()) {
            summary.append("금액: ");
            if (minAmount != null) {
                summary.append(minAmount).append("원 이상 ");
            }
            if (maxAmount != null) {
                summary.append(maxAmount).append("원 이하 ");
            }
        }

        if (hasDateCondition()) {
            summary.append("기간: ");
            if (startDate != null) {
                summary.append(startDate).append(" 이후 ");
            }
            if (endDate != null) {
                summary.append(endDate).append(" 이전 ");
            }
        }

        return summary.toString().trim();
    }

    // 빈 검색 조건 생성
    public static PaymentSearchDto empty() {
        return PaymentSearchDto.builder().build();
    }

    // 모든 조건 검색
    public static PaymentSearchDto all(String userId, String merchantId, String couponId,
        PaymentStatus status, BigDecimal minAmount, BigDecimal maxAmount,
        LocalDateTime startDate, LocalDateTime endDate) {
        return PaymentSearchDto.builder()
            .userId(userId)
            .merchantId(merchantId)
            .couponId(couponId)
            .status(status)
            .minAmount(minAmount)
            .maxAmount(maxAmount)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }
}
