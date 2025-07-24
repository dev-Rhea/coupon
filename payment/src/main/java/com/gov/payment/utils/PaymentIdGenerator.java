package com.gov.payment.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentIdGenerator {

    private static final String PREFIX = "PAY";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static final Random RANDOM = new Random();

    /**
     * 기본 결제 ID 생성 (타임스탬프 + UUID)
     * 형태: PAY_20250125143022_A1B2C3D4
     */
    public String generate() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String paymentId = String.format("%s_%s_%s", PREFIX, timestamp, randomSuffix);

        log.debug("결제 ID 생성: {}", paymentId);
        return paymentId;
    }

    /**
     * 시퀀스 기반 결제 ID 생성 (고성능)
     * 형태: PAY_20250125143022_000001
     */
    public String generateWithSequence() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        long sequence = SEQUENCE.incrementAndGet() % 1000000; // 6자리 시퀀스
        String paymentId = String.format("%s_%s_%06d", PREFIX, timestamp, sequence);

        log.debug("시퀀스 기반 결제 ID 생성: {}", paymentId);
        return paymentId;
    }

    /**
     * 사용자 ID 포함 결제 ID 생성
     * 형태: PAY_USER001_20250125143022_A1B2
     */
    public String generateWithUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return generate();
        }

        String cleanUserId = userId.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (cleanUserId.length() > 10) {
            cleanUserId = cleanUserId.substring(0, 10);
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        String paymentId = String.format("%s_%s_%s_%s", PREFIX, cleanUserId, timestamp, randomSuffix);

        log.debug("사용자 ID 포함 결제 ID 생성: userId={}, paymentId={}", userId, paymentId);
        return paymentId;
    }

    /**
     * 테스트용 결제 ID 생성
     * 형태: PAY_TEST_001
     */
    public String generateForTest() {
        int testNumber = RANDOM.nextInt(1000) + 1;
        String paymentId = String.format("%s_TEST_%03d", PREFIX, testNumber);

        log.debug("테스트용 결제 ID 생성: {}", paymentId);
        return paymentId;
    }

    /**
     * 결제 ID 유효성 검증
     */
    public boolean isValidPaymentId(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return false;
        }

        // 기본 패턴: PAY_로 시작하고 최소 길이 확인
        return paymentId.startsWith(PREFIX + "_") && paymentId.length() >= 10;
    }

    /**
     * 결제 ID에서 타임스탬프 추출
     */
    public LocalDateTime extractTimestamp(String paymentId) {
        if (!isValidPaymentId(paymentId)) {
            return null;
        }

        try {
            String[] parts = paymentId.split("_");
            if (parts.length >= 3) {
                String timestampStr = parts[1];
                if (timestampStr.length() == 14) { // yyyyMMddHHmmss
                    return LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
                }
            }
        } catch (Exception e) {
            log.warn("결제 ID에서 타임스탬프 추출 실패: paymentId={}", paymentId, e);
        }

        return null;
    }

}
