package com.gov.payment.repository;

import com.gov.payment.dto.DailySettlementSummary;
import com.gov.payment.dto.MonthlySettlementSummary;
import com.gov.payment.entity.Settlement;
import com.gov.payment.entity.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends JpaRepository<Settlement, String> {

    // 가맹점별 정산 조회
    List<Settlement> findByMerchant_MerchantIdOrderBySettlementDateDesc(String merchantId);

    // 가맹점과 날짜로 정산 조회
    Optional<Settlement> findByMerchant_MerchantIdAndSettlementDate(String merchantId, LocalDate settlementDate);

    // 상태별 정산 조회
    List<Settlement> findByStatusOrderByCreatedAtDesc(SettlementStatus status);

    // 가맹점별 상태별 정산 조회
    List<Settlement> findByMerchant_MerchantIdAndStatusOrderBySettlementDateDesc(String merchantId, SettlementStatus status);

    // 특정 기간의 정산 조회
    @Query("SELECT s FROM Settlement s WHERE s.settlementDate BETWEEN :startDate AND :endDate " +
        "ORDER BY s.settlementDate DESC")
    List<Settlement> findBySettlementDateBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    // 가맹점별 특정 기간의 정산 조회
    @Query("SELECT s FROM Settlement s WHERE s.merchant.merchantId = :merchantId " +
        "AND s.settlementDate BETWEEN :startDate AND :endDate " +
        "ORDER BY s.settlementDate DESC")
    List<Settlement> findByMerchantAndDateRange(
        @Param("merchantId") String merchantId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    // 승인 대기 중인 정산들
    List<Settlement> findByStatusOrderByCreatedAtAsc(SettlementStatus status);

    // 승인된 정산들
    @Query("SELECT s FROM Settlement s WHERE s.status =: status " +
        "ORDER BY s.approvedAt DESC")
    List<Settlement> findApprovedSettlements(@Param("status") SettlementStatus status);

    // 완료된 정산들
    @Query("SELECT s FROM Settlement s WHERE s.status =:status " +
        "ORDER BY s.transferredAt DESC")
    List<Settlement> findCompletedSettlements(@Param("status") SettlementStatus status);

    // 통계용 메서드들
    @Query("SELECT COUNT(s) FROM Settlement s WHERE s.merchant.merchantId = :merchantId " +
        "AND s.status = :status")
    Long countByMerchantIdAndStatus(@Param("merchantId") String merchantId, @Param("status") SettlementStatus status);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Settlement s WHERE s.merchant.merchantId = :merchantId " +
        "AND s.status = 'COMPLETED'")
    BigDecimal sumTotalAmountByMerchantIdAndCompleted(@Param("merchantId") String merchantId);

    @Query("SELECT COALESCE(SUM(s.netAmount), 0) FROM Settlement s WHERE s.merchant.merchantId = :merchantId " +
        "AND s.status = 'COMPLETED'")
    BigDecimal sumNetAmountByMerchantIdAndCompleted(@Param("merchantId") String merchantId);

    // 기간별 통계
    @Query("SELECT COUNT(s) FROM Settlement s WHERE s.status = 'COMPLETED' " +
        "AND s.settlementDate BETWEEN :startDate AND :endDate")
    Long countCompletedSettlementsBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Settlement s WHERE s.status = 'COMPLETED' " +
        "AND s.settlementDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountCompletedBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(s.netAmount), 0) FROM Settlement s WHERE s.status = 'COMPLETED' " +
        "AND s.settlementDate BETWEEN :startDate AND :endDate")
    BigDecimal sumNetAmountCompletedBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    // 월별 정산 요약
    @Query("SELECT YEAR(s.settlementDate) as year, MONTH(s.settlementDate) as month, " +
        "COUNT(s) as settlementCount, " +
        "COALESCE(SUM(s.totalAmount), 0) as totalAmount, " +
        "COALESCE(SUM(s.netAmount), 0) as netAmount " +
        "FROM Settlement s " +
        "WHERE s.merchant.merchantId = :merchantId " +
        "AND s.status = 'COMPLETED' " +
        "AND s.settlementDate BETWEEN :startDate AND :endDate " +
        "GROUP BY YEAR(s.settlementDate), MONTH(s.settlementDate) " +
        "ORDER BY YEAR(s.settlementDate) DESC, MONTH(s.settlementDate) DESC")
    List<MonthlySettlementSummary> findMonthlySettlementSummary(
        @Param("merchantId") String merchantId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // 일별 정산 현황
    @Query("SELECT s.settlementDate, COUNT(s) as settlementCount, " +
        "COALESCE(SUM(s.totalAmount), 0) as totalAmount, " +
        "COALESCE(SUM(s.netAmount), 0) as netAmount " +
        "FROM Settlement s " +
        "WHERE s.settlementDate BETWEEN :startDate AND :endDate " +
        "GROUP BY s.settlementDate " +
        "ORDER BY s.settlementDate DESC")
    List<DailySettlementSummary> findDailySettlementSummary(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // 최근 정산 조회
    @Query("SELECT s FROM Settlement s ORDER BY s.createdAt DESC")
    List<Settlement> findRecentSettlements();

    // 정산 상세가 없는 정산 조회 (데이터 정합성 체크용)
    @Query("SELECT s FROM Settlement s WHERE s.settlementDetails IS EMPTY " +
        "ORDER BY s.createdAt DESC")
    List<Settlement> findSettlementsWithoutDetails();

    // 데이터 불일치 정산 조회
    @Query("SELECT s FROM Settlement s WHERE " +
        "s.totalAmount != (SELECT COALESCE(SUM(sd.amount), 0) FROM SettlementDetail sd WHERE sd.settlement = s) " +
        "OR s.commissionAmount != (SELECT COALESCE(SUM(sd.commissionAmount), 0) FROM SettlementDetail sd WHERE sd.settlement = s)")
    List<Settlement> findInconsistentSettlements();

    // 장기간 대기 중인 정산 조회 (알림용)
    @Query("SELECT s FROM Settlement s WHERE s.status = 'PENDING' " +
        "AND s.createdAt < :beforeDate " +
        "ORDER BY s.createdAt ASC")
    List<Settlement> findLongPendingSettlements(@Param("beforeDate") LocalDateTime beforeDate);
}
