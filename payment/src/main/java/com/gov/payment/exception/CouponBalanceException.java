package com.gov.payment.exception;

public class CouponBalanceException extends PaymentException {

    public CouponBalanceException(String message) {
        super(message, "COUPON_BALANCE_ERROR");
    }

    public CouponBalanceException(String message, Throwable cause) {
        super(message, "COUPON_BALANCE_ERROR", cause);
    }
}