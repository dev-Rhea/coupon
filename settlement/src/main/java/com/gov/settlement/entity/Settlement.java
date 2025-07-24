package com.gov.settlement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor
public class Settlement {

    @Id
    @Column(name = "settlement_id")
    private String settlementId;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Settlement(String settlementId, String merchantId, LocalDate settlementDate,
        BigDecimal totalAmount, Integer transactionCount) {
        this.settlementId = settlementId;
        this.merchantId = merchantId;
        this.settlementDate = settlementDate;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 상태 변경 메서드 (비즈니스 로직 포함)
    public void updateStatus(SettlementStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void setStatus(SettlementStatus settlementStatus) {
    }
}
