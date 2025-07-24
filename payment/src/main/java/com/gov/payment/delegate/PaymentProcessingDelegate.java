package com.gov.payment.delegate;

import com.gov.payment.entity.PaymentStatus;
import com.gov.payment.service.MockPgService;
import com.gov.payment.service.PaymentService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("paymentProcessingDelegate")
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingDelegate implements JavaDelegate{

    private final MockPgService mockPgService;
    private final PaymentService paymentService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String paymentId = (String) execution.getVariable("paymentId");
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");

        log.info("PG 결제 처리 시작: paymentId={}, amount={}", paymentId, amount);

        // Mock PG 결제 처리
        MockPgService.PgResult result = mockPgService.processPayment(paymentId, amount);

        if (result.success()) {
            // 결제 성공
            paymentService.updatePaymentStatus(
                paymentId,
                PaymentStatus.COMPLETED,
                result.transactionId(),
                null
            );
            execution.setVariable("paymentSuccess", true);
            execution.setVariable("pgTransactionId", result.transactionId());
            log.info("PG 결제 성공: paymentId={}, pgTransactionId={}", paymentId, result.transactionId());
        } else {
            // 결제 실패
            paymentService.updatePaymentStatus(
                paymentId,
                PaymentStatus.FAILED,
                null,
                result.errorMessage()
            );
            execution.setVariable("paymentSuccess", false);
            execution.setVariable("paymentError", result.errorMessage());
            log.error("PG 결제 실패: paymentId={}, error={}", paymentId, result.errorMessage());
        }
    }

}
