package com.gov.payment.delegate;

import com.gov.payment.event.PaymentFailedEvent;
import com.gov.payment.service.MetricsService;
import com.gov.payment.service.NotificationService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("paymentCompletedListener")
@RequiredArgsConstructor
@Slf4j
public class PaymentFailureDelegate implements JavaDelegate {
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final MetricsService metricsService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String paymentId = (String) execution.getVariable("paymentId");
        String userId = (String) execution.getVariable("userId");
        String merchantId = (String) execution.getVariable("merchantId");
        String couponId = (String) execution.getVariable("couponId");
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        String validationError = (String) execution.getVariable("validationError");
        String paymentError = (String) execution.getVariable("paymentError");

        // 실패 사유 결정
        String failureReason = determineFailureReason(validationError, paymentError);

        log.info("결제 실패 후속 처리 시작: paymentId={}, reason={}", paymentId, failureReason);

        try {
            // 1. 결제 실패 이벤트 발행
            PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                this, paymentId, userId, merchantId, couponId, amount, failureReason
            );
            eventPublisher.publishEvent(failedEvent);

            // 2. 사용자 실패 알림
            notificationService.sendPaymentFailureNotification(paymentId, failureReason);

            // 3. 실패 메트릭 기록
            metricsService.recordPaymentFailure(paymentId, failureReason);

            // 4. 재시도 가능 여부 판단
            boolean retryPossible = determineRetryPossibility(failureReason);

            // 5. 실행 변수 설정
            execution.setVariable("failureProcessed", true);
            execution.setVariable("retryPossible", retryPossible);
            execution.setVariable("finalFailureReason", failureReason);

            log.info("결제 실패 후속 처리 완료: paymentId={}, retryPossible={}",
                paymentId, retryPossible);

        } catch (Exception e) {
            log.error("결제 실패 후속 처리 실패: paymentId={}", paymentId, e);

            execution.setVariable("failureProcessed", false);
            execution.setVariable("retryPossible", false);
        }
    }

    /**
     * 실패 사유 결정
     */
    private String determineFailureReason(String validationError, String paymentError) {
        if (validationError != null && !validationError.trim().isEmpty()) {
            return validationError;
        }
        if (paymentError != null && !paymentError.trim().isEmpty()) {
            return paymentError;
        }
        return "알 수 없는 오류";
    }

    /**
     * 재시도 가능 여부 판단
     */
    private boolean determineRetryPossibility(String failureReason) {
        if (failureReason == null) {
            return false;
        }

        // 재시도 가능한 경우들
        if (failureReason.contains("통신 오류") ||
            failureReason.contains("일시적") ||
            failureReason.contains("타임아웃")) {
            return true;
        }

        // 재시도 불가능한 경우들
        return !failureReason.contains("잔액 부족") &&
            !failureReason.contains("만료") &&
            !failureReason.contains("승인 거절");

        // 기본적으로 재시도 가능으로 처리
    }
}