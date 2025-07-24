package com.gov.core.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis 기반 쿠폰 잔액 실시간 관리 서비스
 * 분산 락을 사용하여 동시성 제어
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponBalanceService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    private static final String BALANCE_KEY_PREFIX = "coupon:balance:";
    private static final String LOCK_KEY_PREFIX = "coupon:lock:";
    private static final int LOCK_WAIT_TIME = 5;
    private static final int LOCK_LEASE_TIME = 10;

    /**
     * 쿠폰 잔액을 Redis에 초기화
     */
    public void initializeBalance(String couponId, BigDecimal amount) {
        String key = BALANCE_KEY_PREFIX + couponId;
        redisTemplate.opsForValue().set(key, amount.toString(), Duration.ofHours(24));
        log.debug("쿠폰 잔액 초기화: couponId={}, amount={}", couponId, amount);
    }

    /**
     * 쿠폰 잔액 조회
     */
    public BigDecimal getBalance(String couponId) {
        String key = BALANCE_KEY_PREFIX + couponId;
        String balance = redisTemplate.opsForValue().get(key);

        if (balance == null) {
            log.warn("Redis에서 쿠폰 잔액을 찾을 수 없음: couponId={}", couponId);
            return BigDecimal.ZERO;
        }

        return new BigDecimal(balance);
    }

    /**
     * 쿠폰 금액 예약 (분산 락 사용)
     * 결제 시작 시 호출
     */
    public boolean reserveAmount(String couponId, BigDecimal amount) {
        String lockKey = LOCK_KEY_PREFIX + couponId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS)) {
                String balanceKey = BALANCE_KEY_PREFIX + couponId;
                String currentBalance = redisTemplate.opsForValue().get(balanceKey);

                if (currentBalance == null) {
                    log.warn("쿠폰 잔액 정보가 없음: couponId={}", couponId);
                    return false;
                }

                BigDecimal balance = new BigDecimal(currentBalance);
                if (balance.compareTo(amount) >= 0) {
                    BigDecimal newBalance = balance.subtract(amount);
                    redisTemplate.opsForValue().set(balanceKey, newBalance.toString());

                    log.info("쿠폰 금액 예약 성공: couponId={}, amount={}, remainingBalance={}",
                        couponId, amount, newBalance);
                    return true;
                } else {
                    log.warn("쿠폰 잔액 부족: couponId={}, requestAmount={}, currentBalance={}",
                        couponId, amount, balance);
                    return false;
                }
            } else {
                log.warn("쿠폰 락 획득 실패: couponId={}", couponId);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("쿠폰 예약 중 인터럽트 발생: couponId={}", couponId, e);
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 쿠폰 금액 확정 사용
     * 결제 완료 시 호출
     */
    public void confirmUsage(String couponId, BigDecimal amount) {
        log.info("쿠폰 사용 확정: couponId={}, amount={}", couponId, amount);
    }

    /**
     * 쿠폰 금액 복원
     * 결제 실패 시 호출
     */
    public void restoreAmount(String couponId, BigDecimal amount) {
        String lockKey = LOCK_KEY_PREFIX + couponId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS)) {
                String balanceKey = BALANCE_KEY_PREFIX + couponId;
                String currentBalance = redisTemplate.opsForValue().get(balanceKey);

                if (currentBalance != null) {
                    BigDecimal balance = new BigDecimal(currentBalance);
                    BigDecimal newBalance = balance.add(amount);
                    redisTemplate.opsForValue().set(balanceKey, newBalance.toString());

                    log.info("쿠폰 금액 복원 완료: couponId={}, amount={}, newBalance={}",
                        couponId, amount, newBalance);
                } else {
                    log.warn("복원할 쿠폰 잔액 정보가 없음: couponId={}", couponId);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("쿠폰 복원 중 인터럽트 발생: couponId={}", couponId, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 쿠폰 잔액 동기화 (DB → Redis)
     */
    public void syncBalance(String couponId, BigDecimal dbBalance) {
        String key = BALANCE_KEY_PREFIX + couponId;
        redisTemplate.opsForValue().set(key, dbBalance.toString(), Duration.ofHours(24));
        log.debug("쿠폰 잔액 동기화: couponId={}, balance={}", couponId, dbBalance);
    }

    /**
     * 쿠폰 잔액 캐시 삭제
     */
    public void clearBalance(String couponId) {
        String key = BALANCE_KEY_PREFIX + couponId;
        redisTemplate.delete(key);
        log.debug("쿠폰 잔액 캐시 삭제: couponId={}", couponId);
    }

}
