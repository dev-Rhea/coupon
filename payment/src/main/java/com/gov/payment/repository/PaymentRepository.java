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

    // 사용자별 결제 내역 조회
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);

    // 가맹점별 결제 내역 조회
    List<Payment> findByMerchantIdOrderByCreatedAtDesc(String merchantId);

    // 특정 상태의 결제 건 조회
    List<Payment> findByStatus(PaymentStatus status);

    // 프로세스 인스턴스 ID로 조회
    Optional<Payment> findByProcessInstanceId(String processInstanceId);

    // 정산용 조회 메서드들
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' " +
        "AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findCompletedPaymentsBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.merchantId = :merchantId " +
        "AND p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findSettlementTargetPayments(
        @Param("merchantId") String merchantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // 통계용 메서드들
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.merchantId = :merchantId AND p.status = :status")
    Long countByMerchantIdAndStatus(@Param("merchantId") String merchantId, @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.merchantId = :merchantId AND p.status = 'COMPLETED'")
    BigDecimal sumAmountByMerchantIdAndCompleted(@Param("merchantId") String merchantId);

    // 검색용 메서드들
    @Query("SELECT p FROM Payment p WHERE " +
        "(:userId IS NULL OR p.userId = :userId) AND " +
        "(:merchantId IS NULL OR p.merchantId = :merchantId) AND " +
        "(:status IS NULL OR p.status = :status) AND " +
        "(:minAmount IS NULL OR p.amount >= :minAmount) AND " +
        "(:maxAmount IS NULL OR p.amount <= :maxAmount) AND " +
        "(:startDate IS NULL OR p.createdAt >= :startDate) AND " +
        "(:endDate IS NULL OR p.createdAt <= :endDate) " +
        "ORDER BY p.createdAt DESC")
    List<Payment> findBySearchConditions(
        @Param("userId") String userId,
        @Param("merchantId") String merchantId,
        @Param("status") PaymentStatus status,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

}
