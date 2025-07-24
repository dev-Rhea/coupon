package com.gov.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentFailedEvent extends ApplicationEvent {

    private final String paymentId;
    private final String userId;
    private final String merchantId;
    private final String couponId;
    private final BigDecimal amount;
    private final String failureReason;
    private final LocalDateTime failedAt;

    public PaymentFailedEvent(Object source, String paymentId, String userId,
        String merchantId, String couponId, BigDecimal amount,
        String failureReason) {
        super(source);
        this.paymentId = paymentId;
        this.userId = userId;
        this.merchantId = merchantId;
        this.couponId = couponId;
        this.amount = amount;
        this.failureReason = failureReason;
        this.failedAt = LocalDateTime.now();
    }
}
