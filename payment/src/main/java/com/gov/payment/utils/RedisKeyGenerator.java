package com.gov.payment.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisKeyGenerator {

    // 키 프리픽스 상수들
    private static final String COUPON_BALANCE_PREFIX = "coupon:balance:";
    private static final String COUPON_LOCK_PREFIX = "coupon:lock:";
    private static final String PAYMENT_CACHE_PREFIX = "payment:cache:";
    private static final String PAYMENT_TEMP_PREFIX = "payment:temp:";
    private static final String USER_SESSION_PREFIX = "user:session:";
    private static final String MERCHANT_CACHE_PREFIX = "merchant:cache:";
    private static final String DAILY_STATS_PREFIX = "stats:daily:";
    private static final String RATE_LIMIT_PREFIX = "rate:limit:";

    // 네임스페이스
    private static final String NAMESPACE = "gov:payment:";

    // 날짜 포맷터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 쿠폰 잔액 키 생성
     * 형태: gov:payment:coupon:balance:COUPON_001
     */
    public String couponBalanceKey(String couponId) {
        validateInput(couponId, "couponId");
        String key = NAMESPACE + COUPON_BALANCE_PREFIX + couponId;
        log.debug("쿠폰 잔액 키 생성: {}", key);
        return key;
    }

    /**
     * 쿠폰 락 키 생성
     * 형태: gov:payment:coupon:lock:COUPON_001
     */
    public String couponLockKey(String couponId) {
        validateInput(couponId, "couponId");
        String key = NAMESPACE + COUPON_LOCK_PREFIX + couponId;
        log.debug("쿠폰 락 키 생성: {}", key);
        return key;
    }

    /**
     * 결제 캐시 키 생성
     * 형태: gov:payment:payment:cache:PAY_001
     */
    public String paymentCacheKey(String paymentId) {
        validateInput(paymentId, "paymentId");
        String key = NAMESPACE + PAYMENT_CACHE_PREFIX + paymentId;
        log.debug("결제 캐시 키 생성: {}", key);
        return key;
    }

    /**
     * 결제 임시 키 생성 (처리 중인 결제)
     * 형태: gov:payment:payment:temp:PAY_001
     */
    public String paymentTempKey(String paymentId) {
        validateInput(paymentId, "paymentId");
        String key = NAMESPACE + PAYMENT_TEMP_PREFIX + paymentId;
        log.debug("결제 임시 키 생성: {}", key);
        return key;
    }

    /**
     * 사용자 세션 키 생성
     * 형태: gov:payment:user:session:USER_001
     */
    public String userSessionKey(String userId) {
        validateInput(userId, "userId");
        String key = NAMESPACE + USER_SESSION_PREFIX + userId;
        log.debug("사용자 세션 키 생성: {}", key);
        return key;
    }

    /**
     * 가맹점 캐시 키 생성
     * 형태: gov:payment:merchant:cache:MERCHANT_001
     */
    public String merchantCacheKey(String merchantId) {
        validateInput(merchantId, "merchantId");
        String key = NAMESPACE + MERCHANT_CACHE_PREFIX + merchantId;
        log.debug("가맹점 캐시 키 생성: {}", key);
        return key;
    }

    /**
     * 일별 통계 키 생성
     * 형태: gov:payment:stats:daily:20250125
     */
    public String dailyStatsKey(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        String dateStr = date.format(DATE_FORMATTER);
        String key = NAMESPACE + DAILY_STATS_PREFIX + dateStr;
        log.debug("일별 통계 키 생성: {}", key);
        return key;
    }

    /**
     * 가맹점별 일별 통계 키 생성
     * 형태: gov:payment:stats:daily:20250125:MERCHANT_001
     */
    public String dailyStatsByMerchantKey(LocalDate date, String merchantId) {
        validateInput(merchantId, "merchantId");
        if (date == null) {
            date = LocalDate.now();
        }
        String dateStr = date.format(DATE_FORMATTER);
        String key = NAMESPACE + DAILY_STATS_PREFIX + dateStr + ":" + merchantId;
        log.debug("가맹점별 일별 통계 키 생성: {}", key);
        return key;
    }

    /**
     * 사용자 요청 제한 키 생성
     * 형태: gov:payment:rate:limit:USER_001:20250125
     */
    public String rateLimitKey(String userId) {
        validateInput(userId, "userId");
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String key = NAMESPACE + RATE_LIMIT_PREFIX + userId + ":" + dateStr;
        log.debug("요청 제한 키 생성: {}", key);
        return key;
    }

    /**
     * API별 요청 제한 키 생성
     * 형태: gov:payment:rate:limit:api:processPayment:USER_001:202501251430
     */
    public String apiRateLimitKey(String apiName, String userId) {
        validateInput(apiName, "apiName");
        validateInput(userId, "userId");
        String hourMinute = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String key = NAMESPACE + RATE_LIMIT_PREFIX + "api:" + apiName + ":" + userId + ":" + hourMinute;
        log.debug("API 요청 제한 키 생성: {}", key);
        return key;
    }

    /**
     * 패턴 기반 키 검색용 패턴 생성
     */
    public String couponBalancePattern() {
        return NAMESPACE + COUPON_BALANCE_PREFIX + "*";
    }

    public String paymentCachePattern() {
        return NAMESPACE + PAYMENT_CACHE_PREFIX + "*";
    }

    public String userSessionPattern(String userId) {
        validateInput(userId, "userId");
        return NAMESPACE + USER_SESSION_PREFIX + userId + "*";
    }

    public String dailyStatsPattern(LocalDate date) {
        if (date == null) {
            return NAMESPACE + DAILY_STATS_PREFIX + "*";
        }
        String dateStr = date.format(DATE_FORMATTER);
        return NAMESPACE + DAILY_STATS_PREFIX + dateStr + "*";
    }

    /**
     * 키에서 ID 추출
     */
    public String extractCouponId(String couponBalanceKey) {
        if (couponBalanceKey != null && couponBalanceKey.startsWith(NAMESPACE + COUPON_BALANCE_PREFIX)) {
            return couponBalanceKey.substring((NAMESPACE + COUPON_BALANCE_PREFIX).length());
        }
        return null;
    }

    public String extractPaymentId(String paymentCacheKey) {
        if (paymentCacheKey != null && paymentCacheKey.startsWith(NAMESPACE + PAYMENT_CACHE_PREFIX)) {
            return paymentCacheKey.substring((NAMESPACE + PAYMENT_CACHE_PREFIX).length());
        }
        return null;
    }

    /**
     * 키 유효성 검증
     */
    public boolean isValidCouponBalanceKey(String key) {
        return key != null && key.startsWith(NAMESPACE + COUPON_BALANCE_PREFIX);
    }

    public boolean isValidPaymentCacheKey(String key) {
        return key != null && key.startsWith(NAMESPACE + PAYMENT_CACHE_PREFIX);
    }

    /**
     * 입력값 검증
     */
    private void validateInput(String input, String paramName) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + "은(는) null이거나 빈 문자열일 수 없습니다.");
        }

        // 특수문자 제한 (Redis 키에 사용하면 안 되는 문자들)
        if (input.contains(" ") || input.contains("\n") || input.contains("\r") || input.contains("\t")) {
            throw new IllegalArgumentException(paramName + "에 공백 문자나 제어 문자가 포함될 수 없습니다.");
        }
    }

    /**
     * TTL 상수들 (초 단위)
     */
    public static class TTL {
        public static final int COUPON_BALANCE = 86400;        // 24시간
        public static final int PAYMENT_CACHE = 3600;         // 1시간
        public static final int PAYMENT_TEMP = 600;           // 10분
        public static final int USER_SESSION = 1800;         // 30분
        public static final int MERCHANT_CACHE = 7200;       // 2시간
        public static final int DAILY_STATS = 86400 * 7;     // 7일
        public static final int RATE_LIMIT = 86400;          // 24시간
        public static final int API_RATE_LIMIT = 3600;       // 1시간
    }

}
