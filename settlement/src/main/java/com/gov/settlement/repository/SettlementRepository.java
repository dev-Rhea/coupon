package com.gov.settlement.repository;

import com.gov.settlement.entity.Settlement;
import com.gov.settlement.entity.SettlementStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends JpaRepository<Settlement, String> {

    // 가맹점별 정산 내역 조회
    List<Settlement> findByMerchant_MerchantIdOrderBySettlementDateDesc(String merchantId);

    // 특정 날짜의 정산 내역 조회
    List<Settlement> findBySettlementDate(LocalDate settlementDate);

    // 상태별 정산 내역 조회
    List<Settlement> findByStatus(SettlementStatus status);

    // 특정 기간의 정산 내역 조회
    @Query("SELECT s FROM Settlement s WHERE s.settlementDate BETWEEN :startDate AND :endDate ORDER BY s.settlementDate DESC")
    List<Settlement> findByDateRange(@Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    // 가맹점별 특정 날짜 정산 존재 여부 확인
    boolean existsByMerchant_MerchantIdAndSettlementDate(String merchantId, LocalDate settlementDate);

}
