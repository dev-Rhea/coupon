package com.gov.payment.constant;

public final class PaymentConstants {

    private PaymentConstants() {
        // Utility class
    }

    // 결제 상태
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // 알림 타입
    public static final String NOTIFICATION_SUCCESS = "PAYMENT_SUCCESS";
    public static final String NOTIFICATION_FAILURE = "PAYMENT_FAILURE";
    public static final String NOTIFICATION_ADMIN = "ADMIN_ALERT";

    // 실패 사유
    public static final String FAILURE_INSUFFICIENT_BALANCE = "쿠폰 잔액이 부족합니다";
    public static final String FAILURE_PG_REJECTION = "PG 승인 실패";
    public static final String FAILURE_COMMUNICATION = "PG 통신 오류";
    public static final String FAILURE_TIMEOUT = "결제 처리 시간 초과";
    public static final String FAILURE_UNKNOWN = "알 수 없는 오류";

    // 메트릭 이름
    public static final String METRIC_PAYMENT_SUCCESS = "payment.success.total";
    public static final String METRIC_PAYMENT_FAILURE = "payment.failure.total";
    public static final String METRIC_COUPON_VALIDATION_FAILURE = "coupon.validation.failure.total";
    public static final String METRIC_PG_FAILURE = "pg.failure.total";

    // 시간 설정 (초)
    public static final int PAYMENT_TIMEOUT_SECONDS = 30;
    public static final int LOCK_TIMEOUT_SECONDS = 5;
    public static final int CACHE_TTL_SECONDS = 3600;

    // 크기 제한
    public static final int MAX_RETRY_COUNT = 3;
    public static final int LARGE_PAYMENT_THRESHOLD = 100000;
    public static final int BATCH_SIZE = 1000;
}
