package com.gov.settlement.dto;

import com.gov.settlement.entity.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SettlementDto(
    String settlementId,
    String merchantId,
    String merchantName,
    LocalDate settlementDate,
    BigDecimal totalAmount,
    Integer transactionCount,
    SettlementStatus status
) {
}
