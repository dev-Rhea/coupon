package com.gov.payment.repository;

import com.gov.payment.entity.Payment;
import com.gov.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    // 사용자별 결제 내역 조회 (연관관계 반영)
    List<Payment> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    // 가맹점별 결제 내역 조회 (연관관계 반영)
    List<Payment> findByMerchant_MerchantIdOrderByCreatedAtDesc(String merchantId);

    // 특정 상태의 결제 건 조회
    List<Payment> findByStatus(PaymentStatus status);

    // 가맹점과 상태로 조회
    List<Payment> findByMerchant_MerchantIdAndStatus(String merchantId, PaymentStatus status);

    // 사용자와 상태로 조회
    List<Payment> findByUser_UserIdAndStatus(String userId, PaymentStatus status);

    // 쿠폰별 결제 내역 조회
    List<Payment> findByCoupon_CouponIdOrderByCreatedAtDesc(String couponId);

    // 프로세스 인스턴스 ID로 조회
    Optional<Payment> findByProcessInstanceId(String processInstanceId);

    // PG 거래번호로 조회
    Optional<Payment> findByPgTransactionId(String pgTransactionId);

    // 정산용 조회 메서드들
    @Query("SELECT p FROM Payment p WHERE p.status = :status " +
        "AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findCompletedPaymentsBetween(
        @Param("status") PaymentStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.merchant.merchantId = :merchantId " +
        "AND p.status = :status AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findSettlementTargetPayments(
        @Param("merchantId") String merchantId,
        @Param("status") PaymentStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // 특정 날짜의 완료된 결제 조회
    @Query("SELECT p FROM Payment p WHERE p.merchant.merchantId = :merchantId " +
        "AND p.status = 'COMPLETED' " +
        "AND DATE(p.paymentDate) = :paymentDate " +
        "ORDER BY p.paymentDate DESC")
    List<Payment> findDailyCompletedPayments(
        @Param("merchantId") String merchantId,
        @Param("paymentDate") LocalDateTime paymentDate);

    // 정산되지 않은 완료 결제 조회
    @Query("SELECT p FROM Payment p WHERE p.merchant.merchantId = :merchantId " +
        "AND p.status = 'COMPLETED' " +
        "AND p.settlementDetails IS EMPTY " +
        "ORDER BY p.paymentDate DESC")
    List<Payment> findUnsettledCompletedPayments(@Param("merchantId") String merchantId);

    // 통계용 메서드들
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.merchant.merchantId = :merchantId AND p.status = :status")
    Long countByMerchantIdAndStatus(@Param("merchantId") String merchantId, @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.merchant.merchantId = :merchantId AND p.status = 'COMPLETED'")
    BigDecimal sumAmountByMerchantIdAndCompleted(@Param("merchantId") String merchantId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.user.userId = :userId AND p.status = :status")
    Long countByUserIdAndStatus(@Param("userId") String userId, @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.user.userId = :userId AND p.status = 'COMPLETED'")
    BigDecimal sumAmountByUserIdAndCompleted(@Param("userId") String userId);

    // 쿠폰 사용 통계
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.coupon.couponId = :couponId AND p.status = 'COMPLETED'")
    Long countByCouponIdAndCompleted(@Param("couponId") String couponId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.coupon.couponId = :couponId AND p.status = 'COMPLETED'")
    BigDecimal sumAmountByCouponIdAndCompleted(@Param("couponId") String couponId);

    // 기간별 통계
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' " +
        "AND p.paymentDate BETWEEN :startDate AND :endDate")
    Long countCompletedPaymentsBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' " +
        "AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountCompletedPaymentsBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // 검색용 메서드들 (연관관계 반영)
    @Query("SELECT p FROM Payment p WHERE " +
        "(:userId IS NULL OR p.user.userId = :userId) AND " +
        "(:merchantId IS NULL OR p.merchant.merchantId = :merchantId) AND " +
        "(:couponId IS NULL OR p.coupon.couponId = :couponId) AND " +
        "(:status IS NULL OR p.status = :status) AND " +
        "(:minAmount IS NULL OR p.amount >= :minAmount) AND " +
        "(:maxAmount IS NULL OR p.amount <= :maxAmount) AND " +
        "(:startDate IS NULL OR p.createdAt >= :startDate) AND " +
        "(:endDate IS NULL OR p.createdAt <= :endDate) " +
        "ORDER BY p.createdAt DESC")
    List<Payment> findBySearchConditions(
        @Param("userId") String userId,
        @Param("merchantId") String merchantId,
        @Param("couponId") String couponId,
        @Param("status") PaymentStatus status,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 실패 사유별 조회
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' " +
        "AND (:failureReason IS NULL OR p.failureReason LIKE %:failureReason%) " +
        "ORDER BY p.createdAt DESC")
    List<Payment> findFailedPaymentsByReason(@Param("failureReason") String failureReason);

    // 특정 기간 동안 완료되지 않은 결제 조회 (타임아웃 처리용)
    @Query("SELECT p FROM Payment p WHERE p.status IN (:statuses) " +
        "AND p.createdAt < :timeoutDate " +
        "ORDER BY p.createdAt ASC")
    List<Payment> findTimeoutPayments(@Param("timeoutDate") LocalDateTime timeoutDate, @Param("statuses") List<PaymentStatus> statuses);

    // 가맹점의 일별 결제 요약
    @Query("SELECT DATE(p.paymentDate) as paymentDate, " +
        "COUNT(p) as transactionCount, " +
        "COALESCE(SUM(p.amount), 0) as totalAmount " +
        "FROM Payment p " +
        "WHERE p.merchant.merchantId = :merchantId " +
        "AND p.status = 'COMPLETED' " +
        "AND p.paymentDate BETWEEN :startDate AND :endDate " +
        "GROUP BY DATE(p.paymentDate) " +
        "ORDER BY DATE(p.paymentDate) DESC")
    List<Object[]> findDailyPaymentSummary(
        @Param("merchantId") String merchantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 정산 상세가 있는 결제 조회
    @Query("SELECT p FROM Payment p WHERE p.settlementDetails IS NOT EMPTY " +
        "ORDER BY p.paymentDate DESC")
    List<Payment> findPaymentsWithSettlementDetails();

    // 특정 정산에 포함된 결제 조회
    @Query("SELECT p FROM Payment p JOIN p.settlementDetails sd " +
        "WHERE sd.settlement.settlementId = :settlementId " +
        "ORDER BY p.paymentDate DESC")
    List<Payment> findPaymentsBySettlementId(@Param("settlementId") String settlementId);
}
