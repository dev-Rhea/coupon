package com.gov.core.controller;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 애플리케이션 상태 확인용 헬스체크 컨트롤러
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 기본 헬스체크
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "coupon-core");
        health.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(health);
    }

    /**
     * 상세 헬스체크 (DB, Redis 연결 확인)
     */
    @GetMapping("/health/detail")
    public ResponseEntity<Map<String, Object>> detailHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "coupon-core");
        health.put("timestamp", System.currentTimeMillis());

        // Database 연결 확인
        boolean dbStatus = checkDatabaseConnection();
        health.put("database", dbStatus ? "UP" : "DOWN");

        // Redis 연결 확인
        boolean redisStatus = checkRedisConnection();
        health.put("redis", redisStatus ? "UP" : "DOWN");

        // 전체 상태 결정
        boolean overallStatus = dbStatus && redisStatus;
        health.put("status", overallStatus ? "UP" : "DOWN");

        return ResponseEntity.ok(health);
    }

    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkRedisConnection() {
        try {
            redisTemplate.opsForValue().set("health:check", "test");
            String result = redisTemplate.opsForValue().get("health:check");
            redisTemplate.delete("health:check");
            return "test".equals(result);
        } catch (Exception e) {
            return false;
        }
    }

}
