package com.gov.payment.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component("paymentCompletedListener")
@Slf4j
public class PaymentCompletedListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        // 결제 완료 후 최종 정리 작업
        String paymentId = (String) execution.getVariable("paymentId");
        log.info("결제 프로세스 성공 완료: paymentId={}", paymentId);

        // 캐시 정리, 임시 데이터 삭제 등
        cleanupService.cleanupPaymentProcess(paymentId);
    }
}
