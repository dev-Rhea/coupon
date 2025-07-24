package com.gov.payment.listener;

import com.gov.payment.event.PaymentCompletedEvent;
import com.gov.payment.event.PaymentFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentEventListener {

    @EventListener
    @Async
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 수신: paymentId={}, amount={}",
            event.getPaymentId(), event.getAmount());

        try {
            processCompletedPayment(event);

        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 실패: paymentId={}", event.getPaymentId(), e);
        }
    }

    @EventListener
    @Async
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신: paymentId={}, reason={}",
            event.getPaymentId(), event.getFailureReason());

        try {
            processFailedPayment(event);

        } catch (Exception e) {
            log.error("결제 실패 이벤트 처리 실패: paymentId={}", event.getPaymentId(), e);
        }
    }

    private void processCompletedPayment(PaymentCompletedEvent event) {
        // 결제 완료 후 비즈니스 로직 구현
        log.debug("결제 완료 후 처리: paymentId={}", event.getPaymentId());
    }

    private void processFailedPayment(PaymentFailedEvent event) {
        // 결제 실패 후 비즈니스 로직 구현
        log.debug("결제 실패 후 처리: paymentId={}", event.getPaymentId());
    }

}
