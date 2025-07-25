package com.gov.payment.listener;

import com.gov.payment.service.CleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component("paymentFailedListener")
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedListener {

    private final CleanupService cleanupService;

    public void notify(DelegateExecution execution) throws Exception {
        String paymentId = (String) execution.getVariable("paymentId");
        String failureReason = (String) execution.getVariable("finalFailureReason");

        log.info("결제 프로세스 실패 완료: paymentId={}, reason={}", paymentId, failureReason);

        try {
            // 1. 최종 실패 상태 로깅
            logFailureFinalState(execution);

            // 2. 정리 작업 수행
            cleanupService.cleanupPaymentProcess(paymentId);

            // 3. 실패 완료 메트릭
            recordFailureCompletionMetrics(execution);

        } catch (Exception e) {
            log.error("결제 실패 리스너 실행 실패: paymentId={}", paymentId, e);
        }
    }

    private void logFailureFinalState(DelegateExecution execution) {
        String paymentId = (String) execution.getVariable("paymentId");
        String userId = (String) execution.getVariable("userId");
        String merchantId = (String) execution.getVariable("merchantId");
        String failureReason = (String) execution.getVariable("finalFailureReason");
        Boolean retryPossible = (Boolean) execution.getVariable("retryPossible");

        log.info("최종 결제 실패 상태 - paymentId: {}, userId: {}, merchantId: {}, reason: {}, retryPossible: {}",
            paymentId, userId, merchantId, failureReason, retryPossible);
    }

    private void recordFailureCompletionMetrics(DelegateExecution execution) {
        // 실패 프로세스 완료 메트릭 기록
    }

}
