package com.gov.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Slf4j
public class Coupon extends BaseTimeEntity {

    @Id
    @Column(name = "coupon_id", length = 50)
    private String couponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "original_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "remaining_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CouponStatus status = CouponStatus.ACTIVE;

    private static final int DEFAULT_EXPIRY_DAYS = 90;

    @Builder
    public Coupon(String couponId, User user, BigDecimal originalAmount,
        BigDecimal remainingAmount, LocalDate expiryDate) {
        this.couponId = couponId;
        this.user = user;
        this.originalAmount = originalAmount;
        this.remainingAmount = remainingAmount;
        this.expiryDate = expiryDate != null ? expiryDate : LocalDate.now().plusDays(DEFAULT_EXPIRY_DAYS);
    }

    // 비즈니스 로직 메서드들
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isActive() {
        return status == CouponStatus.ACTIVE && !isExpired();
    }

    public boolean canUse(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        return isActive() && remainingAmount.compareTo(amount) >= 0;
    }

    public void use(BigDecimal amount) {
        if (!canUse(amount)) {
            throw new IllegalStateException("쿠폰을 사용할 수 없습니다.");
        }
        this.remainingAmount = this.remainingAmount.subtract(amount);

        if (this.remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = CouponStatus.USED;
        }
    }

    public void refund(BigDecimal amount) {
        this.remainingAmount = this.remainingAmount.add(amount);
        if (this.status == CouponStatus.USED && this.remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = CouponStatus.ACTIVE;
        }
    }

    public void expire() {
        this.status = CouponStatus.EXPIRED;
        this.remainingAmount = BigDecimal.ZERO;
    }

    /**
     * 강제 만료 처리 (배치용)
     */
    public void forceExpire(String reason) {
        BigDecimal expiredAmount = this.remainingAmount;
        expire();
        log.info("쿠폰 기간 만료: couponId={}, reason={}, originalAmount={}, expiredAmount={}",
            couponId, reason, originalAmount, expiredAmount);
    }

    /**
     * 만료 예정 쿠폰 확인 (D - 7)
     */
    public boolean isExpiringSoon() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate) <= 7 && isActive();
    }

    @Getter
    public enum CouponStatus {
        ACTIVE("사용가능"),
        RESERVED("예약중"),
        USED("사용완료"),
        EXPIRED("만료됨"),
        CANCELLED("취소됨");

        private final String description;

        CouponStatus(String description) {
            this.description = description;
        }

    }
}
