package com.gov.payment.entity;

import com.gov.core.entity.BaseTimeEntity;
import com.gov.core.entity.Coupon;
import com.gov.core.entity.Merchant;
import com.gov.core.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @Column(name = "payment_id", length = 50)
    private String paymentId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @OneToOne
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "process_instance_id", length = 100)
    private String processInstanceId; // Camunda 프로세스 ID

    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId; // PG사 거래번호

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SettlementDetail> settlementDetails = new ArrayList<>();

    @Builder
    public Payment(String paymentId, User user, Merchant merchant,
        Coupon coupon, BigDecimal amount) {
        this.paymentId = paymentId;
        this.user = user;
        this.merchant = merchant;
        this.coupon = coupon;
        this.amount = amount;
    }

    public void updatePaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void changeStatus(PaymentStatus status) {
        this.status = status;
        if (status == PaymentStatus.COMPLETED) {
            this.paymentDate = LocalDateTime.now();
        }
    }

    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.paymentDate = LocalDateTime.now();
    }

    public void markAsFailed(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    public void assignProcessInstance(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public void assignPgTransaction(String pgTransactionId) {
        this.pgTransactionId = pgTransactionId;
    }

    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean isProcessing() {
        return this.status == PaymentStatus.PROCESSING;
    }

    public void addSettlementDetail(SettlementDetail detail) {
        this.settlementDetails.add(detail);
        detail.setPayment(this);
    }

    public void removeSettlementDetail(SettlementDetail detail) {
        this.settlementDetails.remove(detail);
        detail.setPayment(null);
    }

    public List<SettlementDetail> getSettlementDetails() {
        return new ArrayList<>(this.settlementDetails);
    }

    public boolean hasSettlementDetails() {
        return !this.settlementDetails.isEmpty();
    }

    public BigDecimal getTotalSettledAmount() {
        return this.settlementDetails.stream()
            .map(SettlementDetail::getAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}