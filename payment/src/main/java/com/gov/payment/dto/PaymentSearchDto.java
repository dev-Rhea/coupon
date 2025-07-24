package com.gov.payment.dto;

import com.gov.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentSearchDto {

    private final String userId;
    private final String merchantId;
    private final PaymentStatus status;
    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    // 정적 팩토리 메서드들
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

    // 빌더 패턴 스타일 메서드들
    public PaymentSearchDto withUserId(String userId) {
        return builder()
            .userId(userId)
            .build();
    }

    public PaymentSearchDto withMerchantId(String merchantId) {
        return builder()
            .merchantId(merchantId)
            .build();
    }

    public PaymentSearchDto withStatus(PaymentStatus status) {
        return builder()
            .status(status)
            .build();
    }

    public PaymentSearchDto withAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return builder()
            .minAmount(minAmount)
            .maxAmount(maxAmount)
            .build();
    }

    public PaymentSearchDto withDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return builder()
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

    public boolean hasStatusCondition() {
        return status != null;
    }

    public boolean hasAmountCondition() {
        return minAmount != null || maxAmount != null;
    }

    public boolean hasDateCondition() {
        return startDate != null || endDate != null;
    }

    // 유효성 검증
    public boolean isValid() {
        // 최소 금액이 최대 금액보다 큰 경우
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            return false;
        }

        // 시작일이 종료일보다 늦은 경우
        return startDate == null || endDate == null || !startDate.isAfter(endDate);
    }
}