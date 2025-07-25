package com.gov.payment.service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponBalanceService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    private static final String BALANCE_KEY_PREFIX = "coupon:balance:";
    private static final String LOCK_KEY_PREFIX = "coupon:lock:";

    /**
     * 쿠폰 잔액 예약 (분산 락 사용)
     */
    public boolean reserveAmount(String couponId, BigDecimal amount) {
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + couponId);

        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                String balanceKey = BALANCE_KEY_PREFIX + couponId;
                String currentBalance = redisTemplate.opsForValue().get(balanceKey);

                if (currentBalance != null) {
                    BigDecimal balance = new BigDecimal(currentBalance);
                    if (balance.compareTo(amount) >= 0) {
                        BigDecimal newBalance = balance.subtract(amount);
                        redisTemplate.opsForValue().set(balanceKey, newBalance.toString());
                        log.info("쿠폰 잔액 예약 성공: couponId={}, amount={}, newBalance={}",
                            couponId, amount, newBalance);
                        return true;
                    }
                }
                log.warn("쿠폰 잔액 부족: couponId={}, requestAmount={}, currentBalance={}",
                    couponId, amount, currentBalance);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("쿠폰 잔액 예약 중 인터럽트: couponId={}", couponId, e);
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }

    /**
     * 쿠폰 잔액 확정 (예약된 금액을 실제 사용으로 확정)
     */
    public void confirmUsage(String couponId, BigDecimal amount) {
        log.info("쿠폰 사용 확정: couponId={}, amount={}", couponId, amount);
        // 이미 reserveAmount에서 차감했으므로 별도 처리 없음
        // 추가 비즈니스 로직이 필요한 경우 여기에 구현
    }

    /**
     * 쿠폰 잔액 복원 (결제 실패 시)
     */
    public void restoreAmount(String couponId, BigDecimal amount) {
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + couponId);

        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                String balanceKey = BALANCE_KEY_PREFIX + couponId;
                String currentBalance = redisTemplate.opsForValue().get(balanceKey);

                if (currentBalance != null) {
                    BigDecimal balance = new BigDecimal(currentBalance);
                    BigDecimal newBalance = balance.add(amount);
                    redisTemplate.opsForValue().set(balanceKey, newBalance.toString());
                    log.info("쿠폰 잔액 복원 완료: couponId={}, amount={}, newBalance={}",
                        couponId, amount, newBalance);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("쿠폰 잔액 복원 중 인터럽트: couponId={}", couponId, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 쿠폰 잔액 조회
     */
    public BigDecimal getBalance(String couponId) {
        String balanceKey = BALANCE_KEY_PREFIX + couponId;
        String balance = redisTemplate.opsForValue().get(balanceKey);
        return balance != null ? new BigDecimal(balance) : BigDecimal.ZERO;
    }
}
