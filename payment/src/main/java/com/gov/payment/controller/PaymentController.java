package com.gov.payment.controller;

import com.gov.payment.dto.PaymentReqDto;
import com.gov.payment.dto.PaymentResDto;
import com.gov.payment.dto.PaymentSearchDto;
import com.gov.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 요청
     */
    @PostMapping
    public ResponseEntity<PaymentResDto> processPayment(@Valid @RequestBody PaymentReqDto request) {
        try {
            log.info("결제 요청 API 호출: {}", request);
            PaymentResDto response = paymentService.processPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("결제 요청 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 결제 상태 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResDto> getPayment(
        @PathVariable String paymentId) {
        try {
            PaymentResDto response = paymentService.getPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("결제 조회 실패: paymentId={}", paymentId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 사용자별 결제 내역 조회
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PaymentResDto>> getUserPayments(
        @PathVariable String userId) {
        try {
            List<PaymentResDto> responses = paymentService.getUserPayments(userId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("사용자 결제 내역 조회 실패: userId={}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 가맹점별 결제 내역 조회
     */
    @GetMapping("/merchants/{merchantId}")
    public ResponseEntity<List<PaymentResDto>> getMerchantPayments(
        @PathVariable String merchantId) {
        try {
            List<PaymentResDto> responses = paymentService.getMerchantPayments(merchantId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("가맹점 결제 내역 조회 실패: merchantId={}", merchantId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 결제 검색
     */
    @PostMapping("/search")
    public ResponseEntity<List<PaymentResDto>> searchPayments(@RequestBody PaymentSearchDto searchDto) {
        try {
            List<PaymentResDto> responses = paymentService.searchPayments(searchDto);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("결제 검색 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 결제 취소
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResDto> cancelPayment(
        @PathVariable String paymentId) {
        try {
            log.info("결제 취소 API 호출: paymentId={}", paymentId);
            PaymentResDto response = paymentService.cancelPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("결제 취소 실패: paymentId={}", paymentId, e);
            return ResponseEntity.badRequest().build();
        }
    }

}
