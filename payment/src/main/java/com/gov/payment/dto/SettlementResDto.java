package com.gov.payment.dto;

import com.gov.payment.entity.Settlement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record SettlementResDto(
    String settlementId,
    String merchantId,
    String merchantName,
    LocalDate settlementDate,
    BigDecimal totalAmount,
    Integer transactionCount,
    BigDecimal commissionRate,
    BigDecimal commissionAmount,
    BigDecimal netAmount,
    String status,
    String statusDisplayName,
    String statusDescription,
    LocalDateTime approvedAt,
    LocalDateTime transferredAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Integer detailCount,
    BigDecimal totalDetailAmount,
    BigDecimal totalDetailCommissionAmount,
    BigDecimal totalDetailNetAmount,
    boolean isDetailAmountConsistent,
    boolean isDetailCommissionConsistent,
    boolean canBeModified,
    boolean isPending,
    boolean isApproved,
    boolean isCompleted,
    boolean isRejected
) {

    public static SettlementResDto from(Settlement settlement) {
        return SettlementResDto.builder()
            .settlementId(settlement.getSettlementId())
            .merchantId(settlement.getMerchantId())
            .merchantName(settlement.getMerchantName())
            .settlementDate(settlement.getSettlementDate())
            .totalAmount(settlement.getTotalAmount())
            .transactionCount(settlement.getTransactionCount())
            .commissionRate(settlement.getCommissionRate())
            .commissionAmount(settlement.getCommissionAmount())
            .netAmount(settlement.getNetAmount())
            .status(settlement.getStatus().name())
            .statusDisplayName(settlement.getStatus().getDisplayName())
            .statusDescription(settlement.getStatus().getDescription())
            .approvedAt(settlement.getApprovedAt())
            .transferredAt(settlement.getTransferredAt())
            .createdAt(settlement.getCreatedAt())
            .updatedAt(settlement.getUpdatedAt())
            .detailCount(settlement.getDetailCount())
            .totalDetailAmount(settlement.getTotalDetailAmount())
            .totalDetailCommissionAmount(settlement.getTotalDetailCommissionAmount())
            .totalDetailNetAmount(settlement.getTotalDetailNetAmount())
            .isDetailAmountConsistent(settlement.isDetailAmountConsistent())
            .isDetailCommissionConsistent(settlement.isDetailCommissionConsistent())
            .canBeModified(settlement.canBeModified())
            .isPending(settlement.isPending())
            .isApproved(settlement.isApproved())
            .isCompleted(settlement.isCompleted())
            .isRejected(settlement.isRejected())
            .build();
    }

    // 간단한 정보만 포함하는 요약 버전
    public static SettlementResDto fromSummary(Settlement settlement) {
        return SettlementResDto.builder()
            .settlementId(settlement.getSettlementId())
            .merchantId(settlement.getMerchantId())
            .merchantName(settlement.getMerchantName())
            .settlementDate(settlement.getSettlementDate())
            .totalAmount(settlement.getTotalAmount())
            .transactionCount(settlement.getTransactionCount())
            .netAmount(settlement.getNetAmount())
            .status(settlement.getStatus().name())
            .statusDisplayName(settlement.getStatus().getDisplayName())
            .createdAt(settlement.getCreatedAt())
            .isPending(settlement.isPending())
            .isApproved(settlement.isApproved())
            .isCompleted(settlement.isCompleted())
            .isRejected(settlement.isRejected())
            .build();
    }

    // 리스트 조회용 최소 정보만 포함
    public static SettlementResDto fromList(Settlement settlement) {
        return SettlementResDto.builder()
            .settlementId(settlement.getSettlementId())
            .merchantId(settlement.getMerchantId())
            .merchantName(settlement.getMerchantName())
            .settlementDate(settlement.getSettlementDate())
            .totalAmount(settlement.getTotalAmount())
            .netAmount(settlement.getNetAmount())
            .status(settlement.getStatus().name())
            .statusDisplayName(settlement.getStatus().getDisplayName())
            .createdAt(settlement.getCreatedAt())
            .build();
    }
}