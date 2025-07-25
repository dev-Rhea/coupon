package com.gov.payment.entity;

import com.gov.core.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlement_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementDetail extends BaseTimeEntity {

    @Id
    @Column(name = "detail_id", length = 50)
    private String detailId;

    @ManyToOne
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "commission_amount", precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "net_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal netAmount;

    @Builder
    public SettlementDetail(String detailId, Settlement settlement, Payment payment,
        BigDecimal amount, BigDecimal commissionRate) {
        this.detailId = detailId;
        this.settlement = settlement;
        this.payment = payment;
        this.amount = amount;

        // 수수료와 순액 자동 계산
        this.commissionAmount = calculateCommissionAmount(amount, commissionRate);
        this.netAmount = calculateNetAmount(amount, this.commissionAmount);
    }

    // 수수료 금액이 직접 제공되는 경우를 위한 생성자
    @Builder(builderMethodName = "builderWithCommission")
    public SettlementDetail(String detailId, Settlement settlement, Payment payment,
        BigDecimal amount, BigDecimal commissionAmount, BigDecimal netAmount) {
        this.detailId = detailId;
        this.settlement = settlement;
        this.payment = payment;
        this.amount = amount;
        this.commissionAmount = commissionAmount;
        this.netAmount = netAmount != null ? netAmount : calculateNetAmount(amount, commissionAmount);
    }

    // 정산 상세 정보 업데이트 (금액 변경 시)
    public void updateAmount(BigDecimal newAmount, BigDecimal commissionRate) {
        if (newAmount == null || newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다.");
        }

        this.amount = newAmount;
        this.commissionAmount = calculateCommissionAmount(newAmount, commissionRate);
        this.netAmount = calculateNetAmount(newAmount, this.commissionAmount);
    }

    // 수수료 정보 직접 업데이트
    public void updateCommissionInfo(BigDecimal commissionAmount) {
        if (commissionAmount == null || commissionAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("수수료 금액은 0 이상이어야 합니다.");
        }
        if (commissionAmount.compareTo(this.amount) > 0) {
            throw new IllegalArgumentException("수수료 금액은 결제 금액을 초과할 수 없습니다.");
        }

        this.commissionAmount = commissionAmount;
        this.netAmount = calculateNetAmount(this.amount, commissionAmount);
    }

    // 정산 및 결제 정보 업데이트
    public void updateRelations(Settlement settlement, Payment payment) {
        if (settlement == null || payment == null) {
            throw new IllegalArgumentException("정산과 결제 정보는 필수입니다.");
        }
        this.settlement = settlement;
        this.payment = payment;
    }

    // 계산 메서드들
    private BigDecimal calculateCommissionAmount(BigDecimal amount, BigDecimal commissionRate) {
        if (amount == null || commissionRate == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateNetAmount(BigDecimal amount, BigDecimal commissionAmount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal commission = commissionAmount != null ? commissionAmount : BigDecimal.ZERO;
        return amount.subtract(commission);
    }

    // 비즈니스 조회 메서드들
    public BigDecimal getEffectiveCommissionAmount() {
        return this.commissionAmount != null ? this.commissionAmount : BigDecimal.ZERO;
    }

    public BigDecimal getCommissionRate() {
        if (this.amount == null || this.amount.equals(BigDecimal.ZERO) || this.commissionAmount == null) {
            return BigDecimal.ZERO;
        }
        return this.commissionAmount.divide(this.amount, 4, RoundingMode.HALF_UP);
    }

    // 검증 메서드들
    public boolean isValidAmount() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasCommission() {
        return this.commissionAmount != null && this.commissionAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNetAmountCorrect() {
        BigDecimal expectedNetAmount = calculateNetAmount(this.amount, this.commissionAmount);
        return this.netAmount != null && this.netAmount.equals(expectedNetAmount);
    }

    // 관계 확인 메서드들
    public boolean belongsToSettlement(String settlementId) {
        return this.settlement != null && this.settlement.getSettlementId().equals(settlementId);
    }

    public boolean isForPayment(String paymentId) {
        return this.payment != null && this.payment.getPaymentId().equals(paymentId);
    }

    public String getSettlementId() {
        return this.settlement != null ? this.settlement.getSettlementId() : null;
    }

    public String getPaymentId() {
        return this.payment != null ? this.payment.getPaymentId() : null;
    }

    // 정산 상세 생성을 위한 정적 팩토리 메서드들
    public static SettlementDetail createFromPayment(String detailId, Settlement settlement,
        Payment payment, BigDecimal amount,
        BigDecimal commissionRate) {
        return SettlementDetail.builder()
            .detailId(detailId)
            .settlement(settlement)
            .payment(payment)
            .amount(amount)
            .commissionRate(commissionRate)
            .build();
    }

    public static SettlementDetail createWithFixedCommission(String detailId, Settlement settlement,
        Payment payment, BigDecimal amount,
        BigDecimal commissionAmount) {
        return SettlementDetail.builderWithCommission()
            .detailId(detailId)
            .settlement(settlement)
            .payment(payment)
            .amount(amount)
            .commissionAmount(commissionAmount)
            .build();
    }
}
