package com.gov.payment.service;

import com.gov.payment.entity.Payment;
import com.gov.payment.entity.Settlement;
import com.gov.payment.entity.SettlementDetail;
import com.gov.payment.repository.PaymentRepository;
import com.gov.payment.repository.SettlementDetailRepository;
import com.gov.payment.repository.SettlementRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Transactional
    public void createSettlementData(String paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));

            log.info("정산 데이터 생성 시작: paymentId={}", paymentId);

            LocalDate today = LocalDate.now();
            Settlement settlement = getOrCreateSettlement(payment.getMerchant().getMerchantId(), today);

            createSettlementDetail(settlement, payment);
            updateSettlementSummary(settlement, payment);

            log.info("정산 데이터 생성 완료: paymentId={}, settlementId={}",
                paymentId, settlement.getSettlementId());

        } catch (Exception e) {
            log.error("정산 데이터 생성 실패: paymentId={}", paymentId, e);
        }
    }

    private Settlement getOrCreateSettlement(String merchantId, LocalDate settlementDate) {
        return settlementRepository.findByMerchantIdAndSettlementDate(merchantId, settlementDate)
            .orElseGet(() -> {
                Settlement newSettlement = new Settlement();
                newSettlement.setSettlementId(generateSettlementId());
                newSettlement.setMerchantId(merchantId);
                newSettlement.setSettlementDate(settlementDate);
                newSettlement.setTotalAmount(BigDecimal.ZERO);
                newSettlement.setTransactionCount(0);
                newSettlement.setCommissionRate(BigDecimal.valueOf(0.03));
                newSettlement.setCommissionAmount(BigDecimal.ZERO);
                newSettlement.setNetAmount(BigDecimal.ZERO);
                newSettlement.setStatus("PENDING");

                return settlementRepository.save(newSettlement);
            });
    }

    private void createSettlementDetail(Settlement settlement, Payment payment) {
        SettlementDetail detail = new SettlementDetail();
        detail.setDetailId(generateSettlementDetailId());
        detail.setSettlementId(settlement.getSettlementId());
        detail.setPaymentId(payment.getPaymentId());
        detail.setAmount(payment.getAmount());

        BigDecimal commissionAmount = payment.getAmount()
            .multiply(settlement.getCommissionRate());
        detail.setCommissionAmount(commissionAmount);
        detail.setNetAmount(payment.getAmount().subtract(commissionAmount));

        settlementDetailRepository.save(detail);
    }

    private void updateSettlementSummary(Settlement settlement, Payment payment) {
        settlement.setTotalAmount(settlement.getTotalAmount().add(payment.getAmount()));
        settlement.setTransactionCount(settlement.getTransactionCount() + 1);

        BigDecimal totalCommission = settlement.getTotalAmount()
            .multiply(settlement.getCommissionRate());
        settlement.setCommissionAmount(totalCommission);
        settlement.setNetAmount(settlement.getTotalAmount().subtract(totalCommission));

        settlementRepository.save(settlement);
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
