package com.gov.payment.listener;

import com.gov.payment.service.CleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component("paymentCompletedListener")
@RequiredArgsConstructor
@Slf4j
public class PaymentCompletedListener {

    private final CleanupService cleanupService;

    public void notify(DelegateExecution execution) throws Exception {
        String paymentId = (String) execution.getVariable("paymentId");

        log.info("결제 프로세스 성공 완료: paymentId={}", paymentId);

        try {
            // 1. 최종 상태 로깅
            logFinalState(execution);

            // 2. 정리 작업 수행
            cleanupService.cleanupPaymentProcess(paymentId);

            // 3. 성공 완료 메트릭
            recordCompletionMetrics(execution);

        } catch (Exception e) {
            log.error("결제 완료 리스너 실행 실패: paymentId={}", paymentId, e);
        }
    }

    private void logFinalState(DelegateExecution execution) {
        String paymentId = (String) execution.getVariable("paymentId");
        String userId = (String) execution.getVariable("userId");
        String merchantId = (String) execution.getVariable("merchantId");
        String pgTransactionId = (String) execution.getVariable("pgTransactionId");

        log.info("최종 결제 완료 상태 - paymentId: {}, userId: {}, merchantId: {}, pgTransactionId: {}",
            paymentId, userId, merchantId, pgTransactionId);
    }

    private void recordCompletionMetrics(DelegateExecution execution) {
        // 프로세스 완료 시간 메트릭 기록
        // 실제로는 Timer.Sample 을 사용하여 정확한 소요시간 측정
    }

}
