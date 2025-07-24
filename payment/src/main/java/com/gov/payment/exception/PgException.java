package com.gov.payment.exception;

public class PgException extends PaymentException {

    public PgException(String message) {
        super(message, "PG_ERROR");
    }

    public PgException(String message, Throwable cause) {
        super(message, "PG_ERROR", cause);
    }
}