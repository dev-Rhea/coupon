package com.gov.settlement.service;

import com.gov.core.entity.Merchant;
import com.gov.core.repository.MerchantRepository;
import com.gov.settlement.dto.SettlementDto;
import com.gov.settlement.entity.Settlement;
import com.gov.settlement.entity.SettlementStatus;
import com.gov.settlement.repository.SettlementRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SettlementService {

    private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);

    private final SettlementRepository settlementRepository;
    private final JdbcTemplate jdbcTemplate;
    private final MerchantRepository merchantRepository;

    /**
     * 일일 정산 처리 (배치용)
     */
    public void processDailySettlement(LocalDate settlementDate) {
        logger.info("일일 정산 처리 시작: {}", settlementDate);

        // 이미 정산된 데이터가 있는지 확인
        List<Settlement> existingSettlements = settlementRepository.findBySettlementDate(settlementDate);
        if (!existingSettlements.isEmpty()) {
            logger.warn("이미 정산된 날짜입니다: {}", settlementDate);
            return;
        }

        // 가맹점별 결제 데이터 집계
        String sql = """
            SELECT 
                p.merchant_id,
                COUNT(*) as transaction_count,
                SUM(p.amount) as total_amount
            FROM payments p 
            WHERE DATE(p.payment_date) = ? 
            AND p.status = 'COMPLETED'
            GROUP BY p.merchant_id
            """;

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, settlementDate);

        // 정산 데이터 생성
        for (Map<String, Object> result : results) {
            String merchantId = (String) result.get("merchant_id");
            Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("가맹점을 찾을 수 없습니다: " + merchantId));
            Integer transactionCount = ((Number) result.get("transaction_count")).intValue();
            BigDecimal totalAmount = (BigDecimal) result.get("total_amount");

            Settlement settlement = new Settlement(
                generateSettlementId(settlementDate, merchant.getMerchantId()),
                merchant,
                settlementDate,
                totalAmount,
                transactionCount
            );

            settlementRepository.save(settlement);
            logger.info("정산 데이터 생성: 가맹점={}, 금액={}, 건수={}",
                merchantId, totalAmount, transactionCount);
        }

        logger.info("일일 정산 처리 완료: {} 건", results.size());
    }

    /**
     * 정산 승인
     */
    public void approveSettlement(String settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new RuntimeException("정산 데이터를 찾을 수 없습니다: " + settlementId));

        if (settlement.getStatus() != SettlementStatus.PENDING) {
            throw new RuntimeException("승인 가능한 상태가 아닙니다: " + settlement.getStatus());
        }

        settlement.setStatus(SettlementStatus.APPROVED);
        settlementRepository.save(settlement);

        logger.info("정산 승인 완료: {}", settlementId);
    }

    /**
     * 정산 완료 처리
     */
    public void completeSettlement(String settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new RuntimeException("정산 데이터를 찾을 수 없습니다: " + settlementId));

        if (settlement.getStatus() != SettlementStatus.APPROVED) {
            throw new RuntimeException("완료 처리 가능한 상태가 아닙니다: " + settlement.getStatus());
        }

        settlement.setStatus(SettlementStatus.COMPLETED);
        settlementRepository.save(settlement);

        logger.info("정산 완료 처리: {}", settlementId);
    }

    /**
     * 가맹점별 정산 내역 조회
     */
    @Transactional(readOnly = true)
    public List<SettlementDto> getSettlementsByMerchant(String merchantId) {
        List<Settlement> settlements = settlementRepository.findByMerchantIdOrderBySettlementDateDesc(merchantId);
        return settlements.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * 특정 날짜 정산 내역 조회
     */
    @Transactional(readOnly = true)
    public List<SettlementDto> getSettlementsByDate(LocalDate settlementDate) {
        List<Settlement> settlements = settlementRepository.findBySettlementDate(settlementDate);
        return settlements.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * 대기 중인 정산 내역 조회
     */
    @Transactional(readOnly = true)
    public List<SettlementDto> getPendingSettlements() {
        List<Settlement> settlements = settlementRepository.findByStatus(SettlementStatus.PENDING);
        return settlements.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * 정산 ID 생성
     */
    private String generateSettlementId(LocalDate date, String merchantId) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("STL_%s_%s_%s", dateStr, merchantId, uuid);
    }

    /**
     * Entity to DTO 변환
     */
    private SettlementDto convertToDto(Settlement settlement) {
        // 가맹점 이름 조회 (간단히 구현)
        String merchantName = getMerchantName(settlement.getMerchant().getMerchantId());

        return new SettlementDto(
            settlement.getSettlementId(),
            settlement.getMerchant().getMerchantId(),
            merchantName,
            settlement.getSettlementDate(),
            settlement.getTotalAmount(),
            settlement.getTransactionCount(),
            settlement.getStatus(),
            settlement.getCreatedAt(),
            settlement.getUpdatedAt()
        );
    }

    /**
     * 가맹점 이름 조회 (Mock 구현)
     */
    private String getMerchantName(String merchantId) {
        try {
            String sql = "SELECT merchant_name FROM merchants WHERE merchant_id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, merchantId);
        } catch (Exception e) {
            return "Unknown Merchant";
        }
    }
}
