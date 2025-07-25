package com.gov.payment.service;

import com.gov.core.entity.Coupon;
import com.gov.core.entity.Merchant;
import com.gov.core.entity.User;
import com.gov.core.repository.CouponRepository;
import com.gov.core.repository.MerchantRepository;
import com.gov.core.repository.UserRepository;
import com.gov.payment.dto.PaymentReqDto;
import com.gov.payment.dto.PaymentResDto;
import com.gov.payment.dto.PaymentSearchDto;
import com.gov.payment.entity.Payment;
import com.gov.payment.entity.PaymentStatus;
import com.gov.payment.repository.PaymentRepository;
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
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final CouponRepository couponRepository;
    private final RuntimeService runtimeService;

    /**
     * 결제 요청 처리 (Camunda 워크플로우 시작)
     */
    public PaymentResDto processPayment(PaymentReqDto request) {
        log.info("결제 요청 시작: userId={}, merchantId={}, couponId={}, amount={}",
            request.userId(), request.merchantId(), request.couponId(), request.amount());

        // 1. 연관 엔티티 조회
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.userId()));

        Merchant merchant = merchantRepository.findById(request.merchantId())
            .orElseThrow(() -> new IllegalArgumentException("가맹점을 찾을 수 없습니다: " + request.merchantId()));

        Coupon coupon = couponRepository.findById(request.couponId())
            .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + request.couponId()));

        // 2. 결제 엔티티 생성
        String paymentId = generatePaymentId();
        Payment payment = Payment.builder()
            .paymentId(paymentId)
            .user(user)
            .merchant(merchant)
            .coupon(coupon)
            .amount(request.amount())
            .build();

        // 3. DB 저장
        paymentRepository.save(payment);

        // 4. Camunda 워크플로우 시작
        Map<String, Object> variables = new HashMap<>();
        variables.put("paymentId", paymentId);
        variables.put("userId", request.userId());
        variables.put("merchantId", request.merchantId());
        variables.put("couponId", request.couponId());
        variables.put("amount", request.amount());

        String processInstanceId = runtimeService
            .startProcessInstanceByKey("PaymentProcess", paymentId, variables)
            .getProcessInstanceId();

        // 5. 프로세스 인스턴스 ID 업데이트
        payment.assignProcessInstance(processInstanceId);
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
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + paymentId));

        return PaymentResDto.from(payment);
    }

    /**
     * 사용자별 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentResDto> getUserPayments(String userId) {
        List<Payment> payments = paymentRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
        return payments.stream()
            .map(PaymentResDto::from)
            .toList();
    }

    /**
     * 가맹점별 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentResDto> getMerchantPayments(String merchantId) {
        List<Payment> payments = paymentRepository.findByMerchant_MerchantIdOrderByCreatedAtDesc(merchantId);
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
            String error = searchDto.getValidationError();
            throw new IllegalArgumentException(error != null ? error : "잘못된 검색 조건입니다");
        }

        List<Payment> payments = paymentRepository.findBySearchConditions(
            searchDto.userId(),
            searchDto.merchantId(),
            searchDto.couponId(),
            searchDto.status(),
            searchDto.minAmount(),
            searchDto.maxAmount(),
            searchDto.startDate(),
            searchDto.endDate()
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
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + paymentId));

        if (!payment.isCompleted()) {
            throw new IllegalStateException("완료된 결제만 취소할 수 있습니다");
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
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + paymentId));

        // 비즈니스 메서드 사용
        switch (status) {
            case COMPLETED -> payment.markAsCompleted();
            case FAILED -> payment.markAsFailed(failureReason);
            case PROCESSING -> payment.changeStatus(PaymentStatus.PROCESSING);
            default -> payment.changeStatus(status);
        }

        if (pgTransactionId != null) {
            payment.assignPgTransaction(pgTransactionId);
        }

        paymentRepository.save(payment);

        log.info("결제 상태 업데이트: paymentId={}, status={}, pgTransactionId={}",
            paymentId, status, pgTransactionId);
    }

    /**
     * 결제 완료 처리
     */
    public void completePayment(String paymentId, String pgTransactionId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + paymentId));

        payment.markAsCompleted();
        if (pgTransactionId != null) {
            payment.assignPgTransaction(pgTransactionId);
        }

        paymentRepository.save(payment);
        log.info("결제 완료 처리: paymentId={}, pgTransactionId={}", paymentId, pgTransactionId);
    }

    /**
     * 결제 실패 처리
     */
    public void failPayment(String paymentId, String failureReason) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + paymentId));

        payment.markAsFailed(failureReason);
        paymentRepository.save(payment);
        log.info("결제 실패 처리: paymentId={}, failureReason={}", paymentId, failureReason);
    }

    /**
     * 정산 가능한 결제 조회
     */
    @Transactional(readOnly = true)
    public List<Payment> getSettlablePayments(String merchantId) {
        return paymentRepository.findByMerchant_MerchantIdAndStatus(merchantId, PaymentStatus.COMPLETED);
    }

    private String generatePaymentId() {
        return "PAY_" + System.currentTimeMillis() + "_" +
            UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

}
