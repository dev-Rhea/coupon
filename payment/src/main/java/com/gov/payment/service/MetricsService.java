package com.gov.payment.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final PaymentMetrics paymentMetrics;

    private final Map<String, Integer> dailyFailureCounts = new ConcurrentHashMap<>();
    private final Map<String, String> commonFailureReasons = new ConcurrentHashMap<>();

    public void recordPaymentFailure(String paymentId, String failureReason) {
        try {
            log.info("결제 실패 메트릭 기록: paymentId={}, reason={}", paymentId, failureReason);

            paymentMetrics.incrementPaymentFailure();

            if (failureReason != null) {
                if (failureReason.contains("쿠폰")) {
                    paymentMetrics.incrementCouponValidationFailure();
                } else if (failureReason.contains("PG")) {
                    paymentMetrics.incrementPgFailure();
                }
            }

            updateDailyFailureStats(failureReason);
            analyzeFailurePattern(paymentId, failureReason);

        } catch (Exception e) {
            log.error("결제 실패 메트릭 기록 실패: paymentId={}", paymentId, e);
        }
    }

    public void recordPaymentSuccess(String paymentId) {
        try {
            log.debug("결제 성공 메트릭 기록: paymentId={}", paymentId);
            paymentMetrics.incrementPaymentSuccess();

        } catch (Exception e) {
            log.error("결제 성공 메트릭 기록 실패: paymentId={}", paymentId, e);
        }
    }

    private void updateDailyFailureStats(String failureReason) {
        String today = LocalDateTime.now().toLocalDate().toString();
        String key = today + ":" + (failureReason != null ? failureReason : "UNKNOWN");

        dailyFailureCounts.merge(key, 1, Integer::sum);
        commonFailureReasons.put(today, getMostCommonFailureReason(today));
    }

    private void analyzeFailurePattern(String paymentId, String failureReason) {
        if (failureReason != null) {
            if (failureReason.contains("PG 통신 오류")) {
                checkPgCommunicationIssue();
            } else if (failureReason.contains("쿠폰 잔액")) {
                checkCouponBalanceIssue();
            }
        }
    }

    private String getMostCommonFailureReason(String date) {
        return dailyFailureCounts.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(date + ":"))
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey().substring(date.length() + 1))
            .orElse("UNKNOWN");
    }

    private void checkPgCommunicationIssue() {
        log.warn("PG 통신 오류 빈발 감지 - 모니터링 필요");
    }

    private void checkCouponBalanceIssue() {
        log.warn("쿠폰 잔액 오류 빈발 감지 - 캐시 동기화 확인 필요");
    }

    public Map<String, Integer> getDailyFailureStats(String date) {
        return dailyFailureCounts.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(date + ":"))
            .collect(java.util.stream.Collectors.toMap(
                entry -> entry.getKey().substring(date.length() + 1),
                Map.Entry::getValue
            ));
    }

}
