package com.gov.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentCompletedEvent extends ApplicationEvent {

    private final String paymentId;
    private final String userId;
    private final String merchantId;
    private final String couponId;
    private final BigDecimal amount;
    private final String pgTransactionId;
    private final LocalDateTime completedAt;

    public PaymentCompletedEvent(Object source, String paymentId, String userId,
        String merchantId, String couponId, BigDecimal amount,
        String pgTransactionId) {
        super(source);
        this.paymentId = paymentId;
        this.userId = userId;
        this.merchantId = merchantId;
        this.couponId = couponId;
        this.amount = amount;
        this.pgTransactionId = pgTransactionId;
        this.completedAt = LocalDateTime.now();
    }
}

