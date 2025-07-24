package com.gov.core.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
        IllegalArgumentException e) {

        log.warn("잘못된 요청 파라미터: {}", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "INVALID_PARAMETER");
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
        IllegalStateException e) {

        log.warn("잘못된 상태 요청: {}", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "INVALID_STATE");
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.CONFLICT.value());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {

        log.error("예상치 못한 오류 발생", e);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "INTERNAL_SERVER_ERROR");
        response.put("message", "서버 내부 오류가 발생했습니다.");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
