package com.gov.core.repository;

import com.gov.core.entity.Coupon;
import com.gov.core.entity.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, String> {

    /**
     * 사용자의 활성 쿠폰 목록 조회
     */
    @Query("SELECT c FROM Coupon c WHERE c.user = :user " +
        "AND c.status = 'ACTIVE' AND c.expiryDate >= :currentDate " +
        "ORDER BY c.expiryDate ASC")
    List<Coupon> findActiveByUserId(@Param("user") User user,
        @Param("currentDate") LocalDate currentDate);

    /**
     * 특정 금액 이상 사용 가능한 쿠폰 조회
     */
    @Query("SELECT c FROM Coupon c WHERE c.user.userId = :userId " +
        "AND c.status = 'ACTIVE' AND c.expiryDate >= :currentDate " +
        "AND c.remainingAmount >= :amount " +
        "ORDER BY c.expiryDate ASC")
    List<Coupon> findUsableByUserIdAndAmount(@Param("userId") String userId,
        @Param("amount") BigDecimal amount,
        @Param("currentDate") LocalDate currentDate);

    /**
     * 만료된 쿠폰 조회 (배치 처리용)
     */
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
        "AND c.expiryDate < :currentDate")
    List<Coupon> findExpiredCoupons(@Param("currentDate") LocalDate currentDate);

    /**
     * 사용자의 총 쿠폰 잔액 조회
     */
    @Query("SELECT COALESCE(SUM(c.remainingAmount), 0) FROM Coupon c " +
        "WHERE c.user.userId = :userId AND c.status = 'ACTIVE' " +
        "AND c.expiryDate >= :currentDate")
    BigDecimal getTotalRemainingAmount(@Param("userId") String userId,
        @Param("currentDate") LocalDate currentDate);

    /**
     * 쿠폰 ID와 사용자 ID로 조회 (보안 검증용)
     */
    Optional<Coupon> findByCouponIdAndUser_UserId(String couponId, String userId);

}
