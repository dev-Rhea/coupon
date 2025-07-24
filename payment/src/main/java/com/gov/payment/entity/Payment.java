package com.gov.payment.entity;

import com.gov.core.entity.Coupon;
import com.gov.core.entity.Merchant;
import com.gov.core.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 생성자
    public Payment(String paymentId, User user, Merchant merchant,
        Coupon coupon, BigDecimal amount) {
        this.paymentId = paymentId;
        this.user = user;
        this.merchant = merchant;
        this.coupon = coupon;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Payment(String paymentId, @NotBlank String s, @NotBlank String merchantId, @NotBlank String couponId, @NotBlank @DecimalMin(value = "0.01") BigDecimal amount) {
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public void setPgTransactionId(String pgTransactionId) {
        this.pgTransactionId = pgTransactionId;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}