package com.gov.core.service;

import com.gov.core.entity.Coupon;
import com.gov.core.repository.CouponRepository;
import com.gov.core.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponServie {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CouponBalanceService balanceService;

    /**
     * 사용자의 활성 쿠폰 목록 조회
     */
    public List<Coupon> getActiveCoupons(String userId) {
        List<Coupon> coupons = couponRepository.findActiveByUserId(userId, LocalDate.now());

        // Redis 잔액과 DB 잔액 동기화
        coupons.forEach(coupon -> {
            BigDecimal redisBalance = balanceService.getBalance(coupon.getCouponId());
            if (redisBalance.compareTo(BigDecimal.ZERO) == 0) {
                // Redis에 잔액이 없으면 DB 기준으로 초기화
                balanceService.initializeBalance(coupon.getCouponId(), coupon.getRemainingAmount());
            }
        });

        log.info("사용자 활성 쿠폰 조회: userId={}, count={}", userId, coupons.size());
        return coupons;
    }

    /**
     * 쿠폰 상세 조회
     */
    public Coupon getCoupon(String couponId, String userId) {
        Coupon coupon = couponRepository.findByCouponIdAndUser_UserId(couponId, userId)
            .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + couponId));

        // Redis 잔액 동기화
        BigDecimal redisBalance = balanceService.getBalance(couponId);
        if (redisBalance.compareTo(BigDecimal.ZERO) == 0) {
            balanceService.initializeBalance(couponId, coupon.getRemainingAmount());
        }

        log.info("쿠폰 상세 조회: couponId={}, userId={}", couponId, userId);
        return coupon;
    }

    /**
     * 특정 금액 사용 가능한 쿠폰 조회
     */
    public List<Coupon> getUsableCoupons(String userId, BigDecimal amount) {
        List<Coupon> coupons = couponRepository.findUsableByUserIdAndAmount(
            userId, amount, LocalDate.now());

        log.info("사용 가능한 쿠폰 조회: userId={}, amount={}, count={}",
            userId, amount, coupons.size());
        return coupons;
    }

    /**
     * 쿠폰 사용 검증
     */
    public boolean validateCouponUsage(String couponId, String userId, BigDecimal amount) {
        try {
            Coupon coupon = getCoupon(couponId, userId);

            // 기본 검증
            if (!coupon.canUse(amount)) {
                log.warn("쿠폰 사용 불가 - 기본 검증 실패: couponId={}, amount={}", couponId, amount);
                return false;
            }

            // Redis 잔액 검증
            BigDecimal redisBalance = balanceService.getBalance(couponId);
            if (redisBalance.compareTo(amount) < 0) {
                log.warn("쿠폰 사용 불가 - Redis 잔액 부족: couponId={}, requestAmount={}, redisBalance={}",
                    couponId, amount, redisBalance);
                return false;
            }

            log.info("쿠폰 사용 검증 성공: couponId={}, amount={}", couponId, amount);
            return true;

        } catch (Exception e) {
            log.error("쿠폰 검증 중 오류 발생: couponId={}, userId={}", couponId, userId, e);
            return false;
        }
    }

    /**
     * 쿠폰 금액 예약 (결제 시작)
     */
    @Transactional
    public boolean reserveCouponAmount(String couponId, String userId, BigDecimal amount) {
        // 쿠폰 존재 및 권한 확인
        Coupon coupon = getCoupon(couponId, userId);

        // Redis에서 금액 예약
        boolean reserved = balanceService.reserveAmount(couponId, amount);

        if (reserved) {
            log.info("쿠폰 금액 예약 성공: couponId={}, userId={}, amount={}",
                couponId, userId, amount);
        } else {
            log.warn("쿠폰 금액 예약 실패: couponId={}, userId={}, amount={}",
                couponId, userId, amount);
        }

        return reserved;
    }

    /**
     * 쿠폰 사용 확정 (결제 완료)
     */
    @Transactional
    public void confirmCouponUsage(String couponId, String userId, BigDecimal amount) {
        Coupon coupon = getCoupon(couponId, userId);

        // DB에서 실제 차감
        coupon.use(amount);
        couponRepository.save(coupon);

        // Redis에서 사용 확정 (이미 예약 시 차감되어 있음)
        balanceService.confirmUsage(couponId, amount);

        log.info("쿠폰 사용 확정 완료: couponId={}, userId={}, amount={}, remainingAmount={}",
            couponId, userId, amount, coupon.getRemainingAmount());
    }

    /**
     * 쿠폰 사용 취소 (결제 실패)
     */
    @Transactional
    public void cancelCouponUsage(String couponId, String userId, BigDecimal amount) {
        Coupon coupon = getCoupon(couponId, userId);

        // DB에서 환불
        coupon.refund(amount);
        couponRepository.save(coupon);

        // Redis에서 금액 복원
        balanceService.restoreAmount(couponId, amount);

        log.info("쿠폰 사용 취소 완료: couponId={}, userId={}, amount={}, remainingAmount={}",
            couponId, userId, amount, coupon.getRemainingAmount());
    }

    /**
     * 사용자의 총 쿠폰 잔액 조회
     */
    public BigDecimal getTotalBalance(String userId) {
        BigDecimal totalBalance = couponRepository.getTotalRemainingAmount(userId, LocalDate.now());
        log.info("사용자 총 쿠폰 잔액: userId={}, totalBalance={}", userId, totalBalance);
        return totalBalance;
    }

    /**
     * 만료된 쿠폰 처리 (배치 작업용)
     */
    @Transactional
    public int expireOldCoupons() {
        List<Coupon> expiredCoupons = couponRepository.findExpiredCoupons(LocalDate.now());

        for (Coupon coupon : expiredCoupons) {
            coupon.expire();
            balanceService.clearBalance(coupon.getCouponId());
        }

        couponRepository.saveAll(expiredCoupons);

        log.info("만료된 쿠폰 처리 완료: count={}", expiredCoupons.size());
        return expiredCoupons.size();
    }

    /**
     * 쿠폰과 Redis 잔액 동기화
     */
    @Transactional
    public void syncCouponBalance(String couponId) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + couponId));

        balanceService.syncBalance(couponId, coupon.getRemainingAmount());
        log.info("쿠폰 잔액 동기화 완료: couponId={}, balance={}", couponId, coupon.getRemainingAmount());
    }

}
