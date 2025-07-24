package com.gov.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    @Column(name = "settlement_id", length = 50)
    private String settlementId;

    @Column(name = "merchant_id", length = 50, nullable = false)
    private String merchantId;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "net_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal netAmount;

    @Column(name = "status", length = 20)
    private String status = "PENDING";

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "transferred_at")
    private LocalDateTime transferredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setSettlementId(String s) {
    }

    public void setMerchantId(String merchantId) {
    }

    public void setSettlementDate(LocalDate settlementDate) {
    }

    public void setTotalAmount(BigDecimal zero) {
    }

    public void setTransactionCount(int i) {
    }

    public void setCommissionRate(BigDecimal bigDecimal) {
    }

    public void setCommissionAmount(BigDecimal zero) {
    }


    public void setNetAmount(BigDecimal zero) {
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
