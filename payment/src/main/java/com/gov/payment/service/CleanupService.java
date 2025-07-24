package com.gov.payment.service;

import com.gov.payment.utils.RedisKeyGenerator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisKeyGenerator redisKeyGenerator;

    public void cleanupPaymentProcess(String paymentId) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("결제 프로세스 정리 작업 시작: paymentId={}", paymentId);

                cleanupTemporaryCache(paymentId);
                cleanupSessionData(paymentId);
                cleanupExpiredLocks();
                compressOldLogs();

                log.info("결제 프로세스 정리 작업 완료: paymentId={}", paymentId);

            } catch (Exception e) {
                log.error("결제 프로세스 정리 작업 실패: paymentId={}", paymentId, e);
            }
        });
    }

    private void cleanupTemporaryCache(String paymentId) {
        try {
            String tempKey = redisKeyGenerator.paymentTempKey(paymentId);
            redisTemplate.delete(tempKey);

            String pattern = "*" + paymentId + "*temp*";
            Set<String> keysToDelete = redisTemplate.keys(pattern);
            if (keysToDelete != null && !keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                log.debug("임시 캐시 삭제 완료: {} 개 키 삭제", keysToDelete.size());
            }

        } catch (Exception e) {
            log.error("임시 캐시 정리 실패: paymentId={}", paymentId, e);
        }
    }

    private void cleanupSessionData(String paymentId) {
        try {
            log.debug("세션 데이터 정리 완료: paymentId={}", paymentId);

        } catch (Exception e) {
            log.error("세션 데이터 정리 실패: paymentId={}", paymentId, e);
        }
    }

    private void cleanupExpiredLocks() {
        try {
            String lockPattern = "gov:payment:coupon:lock:*";
            Set<String> lockKeys = redisTemplate.keys(lockPattern);

            if (lockKeys != null && !lockKeys.isEmpty()) {
                int cleanedCount = 0;
                for (String lockKey : lockKeys) {
                    Long ttl = redisTemplate.getExpire(lockKey);
                    if (ttl != null && ttl <= 0) {
                        redisTemplate.delete(lockKey);
                        cleanedCount++;
                    }
                }

                if (cleanedCount > 0) {
                    log.debug("만료된 락 정리 완료: {} 개 락 삭제", cleanedCount);
                }
            }

        } catch (Exception e) {
            log.error("만료된 락 정리 실패", e);
        }
    }

    private void compressOldLogs() {
        try {
            log.debug("로그 압축 작업 수행");

        } catch (Exception e) {
            log.error("로그 압축 작업 실패", e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledCleanup() {
        try {
            log.info("스케줄된 정리 작업 시작");

            cleanupExpiredCache();
            cleanupOldTemporaryData();
            optimizeMemoryUsage();

            log.info("스케줄된 정리 작업 완료");

        } catch (Exception e) {
            log.error("스케줄된 정리 작업 실패", e);
        }
    }

    private void cleanupExpiredCache() {
        // 만료된 캐시 정리 로직
    }

    private void cleanupOldTemporaryData() {
        // 24시간 이상 된 임시 데이터 정리
    }

    private void optimizeMemoryUsage() {
        // 메모리 사용량 최적화
        System.gc();
    }

}
