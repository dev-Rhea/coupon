package com.gov.payment.delegate;

import com.gov.payment.event.PaymentCompletedEvent;
import com.gov.payment.service.MetricsService;
import com.gov.payment.service.SettlementService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("paymentSuccessDelegate")
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessDelegate implements JavaDelegate {
    private final ApplicationEventPublisher eventPublisher;
    private final SettlementService settlementService;
    private final MetricsService metricsService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String paymentId = (String) execution.getVariable("paymentId");
        String userId = (String) execution.getVariable("userId");
        String merchantId = (String) execution.getVariable("merchantId");
        String couponId = (String) execution.getVariable("couponId");
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        String pgTransactionId = (String) execution.getVariable("pgTransactionId");

        log.info("결제 성공 후속 처리 시작: paymentId={}", paymentId);

        try {
            // 결제 완료 이벤트 발행
            PaymentCompletedEvent completedEvent = new PaymentCompletedEvent(
                this, paymentId, userId, merchantId, couponId, amount, pgTransactionId
            );
            eventPublisher.publishEvent(completedEvent);

            // 정산 데이터 생성
            settlementService.createSettlementData(paymentId);

            // 성공 메트릭 기록
            metricsService.recordPaymentSuccess(paymentId);

            // 실행 변수 설정
            execution.setVariable("successProcessed", true);
            execution.setVariable("eventPublished", true);

            log.info("결제 성공 후속 처리 완료: paymentId={}", paymentId);

        } catch (Exception e) {
            log.error("결제 성공 후속 처리 실패: paymentId={}", paymentId, e);

            // 후속 처리 실패해도 결제는 성공으로 처리
            execution.setVariable("successProcessed", false);
            execution.setVariable("eventPublished", false);
        }
    }
}