package com.gov.payment.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "입력값 검증 실패",
            errors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "PAYMENT_ERROR",
            ex.getMessage(),
            null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // 에러 응답 클래스
    @Getter
    public static class ErrorResponse {
        private final String errorCode;
        private final String message;
        private final Object details;
        private final LocalDateTime timestamp;

        public ErrorResponse(String errorCode, String message, Object details) {
            this.errorCode = errorCode;
            this.message = message;
            this.details = details;
            this.timestamp = LocalDateTime.now();
        }

    }

}
