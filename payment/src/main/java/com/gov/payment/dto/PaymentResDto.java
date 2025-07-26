package com.gov.payment.dto;

import com.gov.payment.entity.Payment;
import com.gov.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PaymentResDto(
    String paymentId,
    String userId,
    String merchantId,
    String couponId,
    BigDecimal amount,
    PaymentStatus status,
    LocalDateTime paymentDate,
    String processInstanceId,
    String pgTransactionId,
    String failureReason
) {

    public static PaymentResDto from(Payment payment) {
        return PaymentResDto.builder()
            .paymentId(payment.getPaymentId())
            .userId(payment.getUser().getUserId())
            .merchantId(payment.getMerchant().getMerchantId())
            .couponId(payment.getCoupon().getCouponId())
            .amount(payment.getAmount())
            .status(payment.getStatus())
            .paymentDate(payment.getPaymentDate())
            .processInstanceId(payment.getProcessInstanceId())
            .pgTransactionId(payment.getPgTransactionId())
            .failureReason(payment.getFailureReason())
            .build();
    }

}
