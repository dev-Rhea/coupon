package com.gov.payment.service;

import com.gov.core.entity.Merchant;
import com.gov.core.repository.MerchantRepository;
import com.gov.payment.dto.SettlementResDto;
import com.gov.payment.entity.Payment;
import com.gov.payment.entity.Settlement;
import com.gov.payment.entity.SettlementDetail;
import com.gov.payment.entity.SettlementStatus;
import com.gov.payment.repository.PaymentRepository;
import com.gov.payment.repository.SettlementDetailRepository;
import com.gov.payment.repository.SettlementRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;
    private final MerchantRepository merchantRepository;

    /**
     * 결제 완료 시 정산 데이터 생성
     */
    @Transactional
    public void createSettlementData(String paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + paymentId));

            if (!payment.isCompleted()) {
                log.warn("완료되지 않은 결제에 대한 정산 데이터 생성 요청: paymentId={}", paymentId);
                return;
            }

            log.info("정산 데이터 생성 시작: paymentId={}", paymentId);

            LocalDate today = LocalDate.now();
            Settlement settlement = getOrCreateSettlement(payment.getMerchant(), today);

            createSettlementDetail(settlement, payment);

            log.info("정산 데이터 생성 완료: paymentId={}, settlementId={}",
                paymentId, settlement.getSettlementId());

        } catch (Exception e) {
            log.error("정산 데이터 생성 실패: paymentId={}", paymentId, e);
            throw new RuntimeException("정산 데이터 생성에 실패했습니다", e);
        }
    }

    /**
     * 일별 정산 배치 처리
     */
    @Transactional
    public void processDailySettlement(String merchantId, LocalDate settlementDate) {
        Merchant merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다: " + merchantId));

        Settlement settlement = getOrCreateSettlement(merchant, settlementDate);

        // 정산 상세에서 집계 데이터 재계산
        settlement.recalculateFromDetails();
        settlementRepository.save(settlement);

        log.info("일별 정산 처리 완료: merchantId={}, settlementDate={}, totalAmount={}",
            merchantId, settlementDate, settlement.getTotalAmount());
    }

    /**
     * 정산 승인
     */
    @Transactional
    public SettlementResDto approveSettlement(String settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다: " + settlementId));

        settlement.approve();
        settlementRepository.save(settlement);

        log.info("정산 승인 완료: settlementId={}", settlementId);
        return SettlementResDto.from(settlement);
    }

    /**
     * 정산 완료 (이체 완료)
     */
    @Transactional
    public SettlementResDto completeSettlement(String settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다: " + settlementId));

        settlement.complete();
        settlementRepository.save(settlement);

        log.info("정산 완료: settlementId={}", settlementId);
        return SettlementResDto.from(settlement);
    }

    /**
     * 정산 거절
     */
    @Transactional
    public SettlementResDto rejectSettlement(String settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다: " + settlementId));

        settlement.reject();
        settlementRepository.save(settlement);

        log.info("정산 거절: settlementId={}", settlementId);
        return SettlementResDto.from(settlement);
    }

    /**
     * 정산 조회
     */
    @Transactional(readOnly = true)
    public SettlementResDto getSettlement(String settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다: " + settlementId));

        return SettlementResDto.from(settlement);
    }

    /**
     * 가맹점별 정산 내역 조회
     */
    @Transactional(readOnly = true)
    public List<SettlementResDto> getMerchantSettlements(String merchantId) {
        List<Settlement> settlements = settlementRepository.findByMerchant_MerchantIdOrderBySettlementDateDesc(merchantId);
        return settlements.stream()
            .map(SettlementResDto::from)
            .toList();
    }

    /**
     * 상태별 정산 조회
     */
    @Transactional(readOnly = true)
    public List<SettlementResDto> getSettlementsByStatus(SettlementStatus status) {
        List<Settlement> settlements = settlementRepository.findByStatusOrderByCreatedAtDesc(status);
        return settlements.stream()
            .map(SettlementResDto::from)
            .toList();
    }

    /**
     * 정산 데이터 수정
     */
    @Transactional
    public SettlementResDto updateSettlementData(String settlementId, BigDecimal totalAmount,
        Integer transactionCount, BigDecimal commissionRate) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다: " + settlementId));

        settlement.updateSettlementData(totalAmount, transactionCount, commissionRate);
        settlementRepository.save(settlement);

        log.info("정산 데이터 수정: settlementId={}", settlementId);
        return SettlementResDto.from(settlement);
    }

    private Settlement getOrCreateSettlement(Merchant merchant, LocalDate settlementDate) {
        return settlementRepository.findByMerchantIdAndSettlementDate(merchant.getMerchantId(), settlementDate)
            .orElseGet(() -> {
                Settlement newSettlement = Settlement.createForMerchant(
                    generateSettlementId(),
                    merchant,
                    settlementDate
                );
                return settlementRepository.save(newSettlement);
            });
    }

    private void createSettlementDetail(Settlement settlement, Payment payment) {
        // 기본 수수료율 (3%)
        BigDecimal commissionRate = settlement.getEffectiveCommissionRate().equals(BigDecimal.ZERO)
            ? new BigDecimal("0.03")
            : settlement.getEffectiveCommissionRate();

        SettlementDetail detail = SettlementDetail.createFromPayment(
            generateSettlementDetailId(),
            settlement,
            payment,
            payment.getAmount(),
            commissionRate
        );

        settlementDetailRepository.save(detail);

        settlement.addSettlementDetail(detail);
        payment.addSettlementDetail(detail);

        // 정산 집계 데이터 재계산
        settlement.recalculateFromDetails();
        settlementRepository.save(settlement);
    }

    /**
     * 정산 상세 조회
     */
    @Transactional(readOnly = true)
    public List<SettlementDetail> getSettlementDetails(String settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다: " + settlementId));

        return settlement.getSettlementDetails();
    }

    /**
     * 정산 데이터 일치성 검증
     */
    @Transactional(readOnly = true)
    public boolean validateSettlementConsistency(String settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다: " + settlementId));

        boolean isConsistent = settlement.isDetailAmountConsistent() &&
            settlement.isDetailCommissionConsistent();

        if (!isConsistent) {
            log.warn("정산 데이터 불일치 발견: settlementId={}", settlementId);
        }

        return isConsistent;
    }

    /**
     * 정산 데이터 재계산
     */
    @Transactional
    public SettlementResDto recalculateSettlement(String settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다: " + settlementId));

        settlement.recalculateFromDetails();
        settlementRepository.save(settlement);

        log.info("정산 데이터 재계산 완료: settlementId={}", settlementId);
        return SettlementResDto.from(settlement);
    }

    private String generateSettlementId() {
        return "SETTLE_" + System.currentTimeMillis() + "_" +
            UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    private String generateSettlementDetailId() {
        return "DETAIL_" + System.currentTimeMillis() + "_" +
            UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}
