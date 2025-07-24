package com.gov.payment.exception;

public class InsufficientCouponBalanceException extends RuntimeException {
    public InsufficientCouponBalanceException(String message) {
        super(message);
    }
}
