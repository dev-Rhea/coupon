package com.gov.payment.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PaymentSummaryDto(
    String merchantId,
    String merchantName,
    Long totalCount,
    BigDecimal totalAmount,
    Long successCount,
    Long failureCount
) {

    public static PaymentSummaryDto of(String merchantId, String merchantName,
        Long totalCount, BigDecimal totalAmount,
        Long successCount, Long failureCount) {
        return PaymentSummaryDto.builder()
            .merchantId(merchantId)
            .merchantName(merchantName)
            .totalCount(totalCount)
            .totalAmount(totalAmount)
            .successCount(successCount)
            .failureCount(failureCount)
            .build();
    }
}
