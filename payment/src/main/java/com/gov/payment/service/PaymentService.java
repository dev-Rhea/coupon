package com.gov.payment.service;

import com.gov.payment.dto.PaymentReqDto;
import com.gov.payment.dto.PaymentResDto;
import com.gov.payment.dto.PaymentSearchDto;
import com.gov.payment.entity.Payment;
import com.gov.payment.entity.PaymentStatus;
import com.gov.payment.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RuntimeService runtimeService;
    private final CouponBalanceService couponBalanceService;

    /**
     * 결제 요청 처리 (Camunda 워크플로우 시작)
     */
    public PaymentResDto processPayment(PaymentReqDto request) {
        log.info("결제 요청 시작: userId={}, merchantId={}, couponId={}, amount={}",
            request.userId(), request.merchantId(), request.couponId(), request.amount());

        // 1. 결제 엔티티 생성
        String paymentId = generatePaymentId();
        Payment payment = new Payment(
            paymentId,
            request.userId(),
            request.merchantId(),
            request.couponId(),
            request.amount()
        );

        // 2. DB 저장
        paymentRepository.save(payment);

        // 3. Camunda 워크플로우 시작
        Map<String, Object> variables = new HashMap<>();
        variables.put("paymentId", paymentId);
        variables.put("userId", request.userId());
        variables.put("merchantId", request.merchantId());
        variables.put("couponId", request.couponId());
        variables.put("amount", request.amount());

        String processInstanceId = runtimeService
            .startProcessInstanceByKey("PaymentProcess", paymentId, variables)
            .getProcessInstanceId();

        // 4. 프로세스 인스턴스 ID 업데이트
        payment.setProcessInstanceId(processInstanceId);
        paymentRepository.save(payment);

        log.info("결제 워크플로우 시작 완료: paymentId={}, processInstanceId={}",
            paymentId, processInstanceId);

        return PaymentResDto.from(payment);
    }

    /**
     * 결제 상태 조회
     */
    @Transactional(readOnly = true)
    public PaymentResDto getPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));

        return PaymentResDto.from(payment);
    }

    /**
     * 사용자별 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentResDto> getUserPayments(String userId) {
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return payments.stream()
            .map(PaymentResDto::from)
            .toList();
    }

    /**
     * 가맹점별 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentResDto> getMerchantPayments(String merchantId) {
        List<Payment> payments = paymentRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
        return payments.stream()
            .map(PaymentResDto::from)
            .toList();
    }

    /**
     * 조건별 결제 검색
     */
    @Transactional(readOnly = true)
    public List<PaymentResDto> searchPayments(PaymentSearchDto searchDto) {
        if (!searchDto.isValid()) {
            throw new IllegalArgumentException("잘못된 검색 조건입니다");
        }

        List<Payment> payments = paymentRepository.findBySearchConditions(
            searchDto.getUserId(),
            searchDto.getMerchantId(),
            searchDto.getStatus(),
            searchDto.getMinAmount(),
            searchDto.getMaxAmount(),
            searchDto.getStartDate(),
            searchDto.getEndDate()
        );

        return payments.stream()
            .map(PaymentResDto::from)
            .toList();
    }

    /**
     * 결제 취소
     */
    public PaymentResDto cancelPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("완료된 결제만 취소할 수 있습니다");
        }

        log.info("결제 취소 요청: paymentId={}", paymentId);

        // Camunda 메시지 전송으로 취소 프로세스 시작
        runtimeService.createMessageCorrelation("PaymentCancelMessage")
            .processInstanceId(payment.getProcessInstanceId())
            .correlate();

        return PaymentResDto.from(payment);
    }

    /**
     * 결제 상태 업데이트 (Camunda Delegate에서 호출)
     */
    public void updatePaymentStatus(String paymentId, PaymentStatus status,
        String pgTransactionId, String failureReason) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));

        payment.setStatus(status);
        if (status == PaymentStatus.COMPLETED) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        if (pgTransactionId != null) {
            payment.setPgTransactionId(pgTransactionId);
        }
        if (failureReason != null) {
            payment.setFailureReason(failureReason);
        }

        paymentRepository.save(payment);

        log.info("결제 상태 업데이트: paymentId={}, status={}, pgTransactionId={}",
            paymentId, status, pgTransactionId);
    }

    private String generatePaymentId() {
        return "PAY_" + System.currentTimeMillis() + "_" +
            UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

}
