package com.gov.payment.entity;

import com.gov.core.entity.BaseTimeEntity;
import com.gov.core.entity.Merchant;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor
public class Settlement extends BaseTimeEntity {

    @Id
    @Column(name = "settlement_id", length = 50)
    private String settlementId;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "transferred_at")
    private LocalDateTime transferredAt;

    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SettlementDetail> settlementDetails = new ArrayList<>();

    @Builder
    public Settlement(String settlementId, Merchant merchant, LocalDate settlementDate,
        BigDecimal totalAmount, Integer transactionCount, BigDecimal commissionRate) {
        this.settlementId = settlementId;
        this.merchant = merchant;
        this.settlementDate = settlementDate;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.commissionRate = commissionRate;
        this.commissionAmount = calculateCommissionAmount(totalAmount, commissionRate);
        this.netAmount = calculateNetAmount(totalAmount, this.commissionAmount);
    }

    public void approve() {
        if (this.status != SettlementStatus.PENDING) {
            throw new IllegalStateException("정산 승인은 PENDING 상태에서만 가능합니다.");
        }
        this.status = SettlementStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status != SettlementStatus.APPROVED) {
            throw new IllegalStateException("정산 완료는 APPROVED 상태에서만 가능합니다.");
        }
        this.status = SettlementStatus.COMPLETED;
        this.transferredAt = LocalDateTime.now();
    }

    public void reject() {
        if (this.status == SettlementStatus.COMPLETED) {
            throw new IllegalStateException("완료된 정산은 거절할 수 없습니다.");
        }
        this.status = SettlementStatus.REJECTED;
    }

    public void updateSettlementData(BigDecimal totalAmount, Integer transactionCount,
        BigDecimal commissionRate) {
        if (this.status != SettlementStatus.PENDING) {
            throw new IllegalStateException("정산 데이터는 PENDING 상태에서만 수정 가능합니다.");
        }

        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.commissionRate = commissionRate;

        // 수수료 금액과 순액 재계산
        this.commissionAmount = calculateCommissionAmount(totalAmount, commissionRate);
        this.netAmount = calculateNetAmount(totalAmount, this.commissionAmount);
    }

    public void updateMerchant(Merchant merchant) {
        if (this.status != SettlementStatus.PENDING) {
            throw new IllegalStateException("상인 정보는 PENDING 상태에서만 수정 가능합니다.");
        }
        this.merchant = merchant;
    }

    public boolean isPending() {
        return this.status == SettlementStatus.PENDING;
    }

    public boolean isApproved() {
        return this.status == SettlementStatus.APPROVED;
    }

    public boolean isCompleted() {
        return this.status == SettlementStatus.COMPLETED;
    }

    public boolean isRejected() {
        return this.status == SettlementStatus.REJECTED;
    }

    public boolean canBeModified() {
        return this.status == SettlementStatus.PENDING;
    }

    private BigDecimal calculateCommissionAmount(BigDecimal totalAmount, BigDecimal commissionRate) {
        if (totalAmount == null || commissionRate == null) {
            return BigDecimal.ZERO;
        }
        return totalAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateNetAmount(BigDecimal totalAmount, BigDecimal commissionAmount) {
        if (totalAmount == null) {
            return BigDecimal.ZERO;
        }
        if (commissionAmount == null) {
            return totalAmount;
        }
        return totalAmount.subtract(commissionAmount);
    }

    public BigDecimal getEffectiveCommissionRate() {
        return this.commissionRate != null ? this.commissionRate : BigDecimal.ZERO;
    }

    public BigDecimal getEffectiveCommissionAmount() {
        return this.commissionAmount != null ? this.commissionAmount : BigDecimal.ZERO;
    }

    public void addSettlementDetail(SettlementDetail detail) {
        if (detail == null) {
            throw new IllegalArgumentException("정산 상세는 null일 수 없습니다.");
        }
        this.settlementDetails.add(detail);
    }

    public void removeSettlementDetail(SettlementDetail detail) {
        this.settlementDetails.remove(detail);
    }

    public List<SettlementDetail> getSettlementDetails() {
        return new ArrayList<>(this.settlementDetails);
    }

    public int getDetailCount() {
        return this.settlementDetails.size();
    }

    public BigDecimal getTotalDetailAmount() {
        return this.settlementDetails.stream()
            .map(SettlementDetail::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalDetailCommissionAmount() {
        return this.settlementDetails.stream()
            .map(SettlementDetail::getEffectiveCommissionAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalDetailNetAmount() {
        return this.settlementDetails.stream()
            .map(SettlementDetail::getNetAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isDetailAmountConsistent() {
        BigDecimal detailTotal = getTotalDetailAmount();
        return this.totalAmount != null && this.totalAmount.equals(detailTotal);
    }

    public boolean isDetailCommissionConsistent() {
        BigDecimal detailCommissionTotal = getTotalDetailCommissionAmount();
        return this.commissionAmount != null && this.commissionAmount.equals(detailCommissionTotal);
    }

    // 상인 관련 조회 메서드들
    public String getMerchantId() {
        return this.merchant != null ? this.merchant.getMerchantId() : null;
    }

    public String getMerchantName() {
        return this.merchant != null ? this.merchant.getMerchantName() : null;
    }

    public boolean belongsToMerchant(String merchantId) {
        return this.merchant != null && this.merchant.getMerchantId().equals(merchantId);
    }

    // 정산 상세에서 집계 데이터 재계산
    public void recalculateFromDetails() {
        if (this.status != SettlementStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 재계산이 가능합니다.");
        }

        this.totalAmount = getTotalDetailAmount();
        this.commissionAmount = getTotalDetailCommissionAmount();
        this.netAmount = getTotalDetailNetAmount();
        this.transactionCount = getDetailCount();

        // 평균 수수료율 재계산
        if (this.totalAmount != null && this.totalAmount.compareTo(BigDecimal.ZERO) > 0 &&
            this.commissionAmount != null) {
            this.commissionRate = this.commissionAmount.divide(this.totalAmount, 4,
                RoundingMode.HALF_UP);
        }
    }

    // 정적 팩토리 메서드
    public static Settlement createForMerchant(String settlementId, Merchant merchant,
        LocalDate settlementDate) {
        return Settlement.builder()
            .settlementId(settlementId)
            .merchant(merchant)
            .settlementDate(settlementDate)
            .totalAmount(BigDecimal.ZERO)
            .transactionCount(0)
            .commissionRate(BigDecimal.ZERO)
            .build();
    }
}
