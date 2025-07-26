package com.gov.core.controller;

import com.gov.core.dto.CouponExpiryResult;
import com.gov.core.service.CouponExpiryService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/batch")
@Slf4j
@RequiredArgsConstructor
public class BatchAdminController {

    private final CouponExpiryService couponExpiryService;

    /**
     * 쿠폰 만료 배치 수동 실행
     */
    @PostMapping("/coupons/expire")
    public ResponseEntity<Map<String, Object>> manualExpireCoupons() {
        log.info("쿠폰 만료 배치 수동 실행 요청");

        try {
            CouponExpiryResult result = couponExpiryService.processExpiredCoupons();

            log.info("쿠폰 만료 배치 수동 실행 완료: {}", result);
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("status", "success");
            successResponse.put("message", "쿠폰 만료 배치가 성공적으로 실행되었습니다");
            successResponse.put("result", result);
            successResponse.put("timeStamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            log.error("쿠폰 만료 배치 수동 실행 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "쿠폰 만료 배치 실행 중 오류 발생, " + e.getMessage());
            errorResponse.put("timeStamp", LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 배치 작업 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBatchStatus() {
        log.info("배치 작업 상태 조회 요청");

        try {
            Map<String, Object> status = new HashMap<>();
            status.put("status", "healthy");
            status.put("message", "배치 시스템이 정상 동작 중입니다");
            status.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("배치 작업 상태 조회 실패", e);

            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("status", "error");
            errorStatus.put("message", "상태 조회 실패");
            errorStatus.put("timestamp", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(errorStatus);
        }
    }

    /**
     * 헬스체크용 간단한 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Batch Admin Controller is healthy");
    }
}
