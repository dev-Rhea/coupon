package com.gov.settlement.entity;

import com.gov.core.entity.BaseTimeEntity;
import com.gov.core.entity.Merchant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Settlement extends BaseTimeEntity {

    @Id
    @Column(name = "settlement_id")
    private String settlementId;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SettlementStatus status = SettlementStatus.PENDING;

    public Settlement(String settlementId, Merchant merchant, LocalDate settlementDate,
        BigDecimal totalAmount, Integer transactionCount) {
        this.settlementId = settlementId;
        this.merchant = merchant;
        this.settlementDate = settlementDate;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    public void setStatus(SettlementStatus settlementStatus) {
        if (this.status != settlementStatus) {
            this.status = settlementStatus;
        }
    }
}
