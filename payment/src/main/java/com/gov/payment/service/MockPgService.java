package com.gov.payment.service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MockPgService {

    private final Random random = new Random();

    /**
     * Mock PG 결제 처리
     */
    public PgResult processPayment(String paymentId, BigDecimal amount) {
        log.info("PG 결제 처리 시작: paymentId={}, amount={}", paymentId, amount);

        try {
            Thread.sleep(100); // PG 처리 시간 시뮬레이션

            boolean success = random.nextDouble() < 0.9; // 90% 성공률

            if (success) {
                String transactionId = generatePgTransactionId();
                log.info("PG 결제 성공: paymentId={}, pgTransactionId={}", paymentId, transactionId);
                return new PgResult(true, transactionId, null);
            } else {
                log.warn("PG 결제 실패: paymentId={}", paymentId);
                return new PgResult(false, null, "PG 승인 실패");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("PG 통신 오류: paymentId={}", paymentId, e);
            return new PgResult(false, null, "PG 통신 오류");
        }
    }

    /**
     * Mock PG 취소/환불 처리
     */
    public PgResult cancelPayment(String pgTransactionId, BigDecimal amount) {
        log.info("PG 결제 취소 시작: pgTransactionId={}, amount={}", pgTransactionId, amount);

        try {
            Thread.sleep(50); // PG 취소 처리 시간

            boolean success = random.nextDouble() < 0.95; // 95% 성공률

            if (success) {
                String cancelTransactionId = generatePgTransactionId();
                log.info("PG 결제 취소 성공: originalId={}, cancelId={}", pgTransactionId, cancelTransactionId);
                return new PgResult(true, cancelTransactionId, null);
            } else {
                log.warn("PG 결제 취소 실패: pgTransactionId={}", pgTransactionId);
                return new PgResult(false, null, "PG 취소 실패");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("PG 취소 통신 오류: pgTransactionId={}", pgTransactionId, e);
            return new PgResult(false, null, "PG 통신 오류");
        }
    }

    private String generatePgTransactionId() {
        return "PG_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
         * PG 결과 클래스
         */
        @Getter
        public record PgResult(boolean success, String transactionId, String errorMessage) {

    }

}
