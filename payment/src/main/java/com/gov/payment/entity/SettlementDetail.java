package com.gov.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlement_details")
@Getter
@NoArgsConstructor
public class SettlementDetail {

    @Id
    @Column(name = "detail_id", length = 50)
    private String detailId;

    @Column(name = "settlement_id", length = 50, nullable = false)
    private String settlementId;

    @Column(name = "payment_id", length = 50, nullable = false)
    private String paymentId;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "commission_amount", precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "net_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal netAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void setDetailId(String s) {
    }

    public void setSettlementId(String settlementId) {
    }

    public void setPaymentId(String paymentId) {
    }

    public void setAmount(BigDecimal amount) {
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
    }

    public void setNetAmount(BigDecimal subtract) {

    }
}
