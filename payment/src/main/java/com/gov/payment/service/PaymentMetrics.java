package com.gov.payment.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class PaymentMetrics {

    private final MeterRegistry meterRegistry;

    // 결제 관련 카운터
    private final Counter paymentSuccessCounter;
    private final Counter paymentFailureCounter;
    private final Counter couponValidationFailureCounter;
    private final Counter pgFailureCounter;

    // 결제 처리 시간 측정
    private final Timer paymentProcessingTimer;

    // 실시간 통계를 위한 게이지
    private final AtomicLong totalPaymentCount = new AtomicLong(0);
    private final AtomicLong totalSuccessCount = new AtomicLong(0);
    private final AtomicLong totalFailureCount = new AtomicLong(0);
    private final AtomicLong totalPaymentAmount = new AtomicLong(0);

    // 일별 통계
    private final AtomicLong todayPaymentCount = new AtomicLong(0);
    private final AtomicLong todaySuccessCount = new AtomicLong(0);
    private final AtomicLong todayFailureCount = new AtomicLong(0);

    private String currentDate;

    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 카운터 초기화 - 직접 생성 방법
        this.paymentSuccessCounter = meterRegistry.counter("payment.success.total",
            "type", "success");

        this.paymentFailureCounter = meterRegistry.counter("payment.failure.total",
            "type", "failure");

        this.couponValidationFailureCounter = meterRegistry.counter("payment.coupon.validation.failure",
            "type", "coupon_failure");

        this.pgFailureCounter = meterRegistry.counter("payment.pg.failure",
            "type", "pg_failure");

        // 타이머 초기화 - 직접 생성 방법
        this.paymentProcessingTimer = meterRegistry.timer("payment.processing.time");

        // 게이지 등록
        registerGauges();

        log.info("PaymentMetrics 초기화 완료");
    }

    private void registerGauges() {
        // 전체 통계 게이지 - 직접 등록 방법
        meterRegistry.gauge("payment.total.count", totalPaymentCount, AtomicLong::get);
        meterRegistry.gauge("payment.success.count", totalSuccessCount, AtomicLong::get);
        meterRegistry.gauge("payment.failure.count", totalFailureCount, AtomicLong::get);
        meterRegistry.gauge("payment.total.amount", totalPaymentAmount, AtomicLong::get);

        // 성공률 게이지
        meterRegistry.gauge("payment.success.rate", this, PaymentMetrics::getSuccessRate);

        // 일별 통계 게이지
        meterRegistry.gauge("payment.today.count", todayPaymentCount, AtomicLong::get);
        meterRegistry.gauge("payment.today.success", todaySuccessCount, AtomicLong::get);
        meterRegistry.gauge("payment.today.failure", todayFailureCount, AtomicLong::get);
        meterRegistry.gauge("payment.today.success.rate", this, PaymentMetrics::getTodaySuccessRate);
    }

    /**
     * 결제 성공 카운트 증가
     */
    public void incrementPaymentSuccess() {
        paymentSuccessCounter.increment();
        totalPaymentCount.incrementAndGet();
        totalSuccessCount.incrementAndGet();

        checkAndResetDailyCounters();
        todayPaymentCount.incrementAndGet();
        todaySuccessCount.incrementAndGet();

        log.debug("결제 성공 메트릭 증가: 총 성공 건수={}", totalSuccessCount.get());
    }

    /**
     * 결제 실패 카운트 증가
     */
    public void incrementPaymentFailure() {
        paymentFailureCounter.increment();
        totalPaymentCount.incrementAndGet();
        totalFailureCount.incrementAndGet();

        checkAndResetDailyCounters();
        todayPaymentCount.incrementAndGet();
        todayFailureCount.incrementAndGet();

        log.debug("결제 실패 메트릭 증가: 총 실패 건수={}", totalFailureCount.get());
    }

    /**
     * 쿠폰 검증 실패 카운트 증가
     */
    public void incrementCouponValidationFailure() {
        couponValidationFailureCounter.increment();
        log.debug("쿠폰 검증 실패 메트릭 증가");
    }

    /**
     * PG 결제 실패 카운트 증가
     */
    public void incrementPgFailure() {
        pgFailureCounter.increment();
        log.debug("PG 결제 실패 메트릭 증가");
    }

    /**
     * 결제 금액 추가
     */
    public void recordPaymentAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            totalPaymentAmount.addAndGet(amount.longValue());
            log.debug("결제 금액 메트릭 추가: amount={}, 총액={}", amount, totalPaymentAmount.get());
        }
    }

    /**
     * 결제 처리 시간 기록 시작
     */
    public Timer.Sample startPaymentTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 결제 처리 시간 종료 및 기록
     */
    public void recordPaymentProcessingTime(Timer.Sample sample) {
        if (sample != null) {
            sample.stop(paymentProcessingTimer);
        }
    }

    /**
     * 결제 처리 시간 직접 기록 (밀리초)
     */
    public void recordPaymentProcessingTime(long milliseconds) {
        paymentProcessingTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * 전체 결제 성공률 계산
     */
    public double getSuccessRate() {
        long total = totalPaymentCount.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalSuccessCount.get() / total * 100.0;
    }

    /**
     * 오늘 결제 성공률 계산
     */
    public double getTodaySuccessRate() {
        long todayTotal = todayPaymentCount.get();
        if (todayTotal == 0) {
            return 0.0;
        }
        return (double) todaySuccessCount.get() / todayTotal * 100.0;
    }

    /**
     * 현재 통계 정보 반환
     */
    public PaymentStatistics getCurrentStatistics() {
        return PaymentStatistics.builder()
            .totalPaymentCount(totalPaymentCount.get())
            .totalSuccessCount(totalSuccessCount.get())
            .totalFailureCount(totalFailureCount.get())
            .totalPaymentAmount(totalPaymentAmount.get())
            .successRate(getSuccessRate())
            .todayPaymentCount(todayPaymentCount.get())
            .todaySuccessCount(todaySuccessCount.get())
            .todayFailureCount(todayFailureCount.get())
            .todaySuccessRate(getTodaySuccessRate())
            .build();
    }

    /**
     * 일자 변경 체크 및 일별 카운터 리셋
     */
    private void checkAndResetDailyCounters() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (!today.equals(currentDate)) {
            log.info("일자 변경 감지: {} -> {}, 일별 카운터 초기화", currentDate, today);
            currentDate = today;
            todayPaymentCount.set(0);
            todaySuccessCount.set(0);
            todayFailureCount.set(0);
        }
    }

    /**
     * 메트릭 리셋 (테스트 및 초기화 용도)
     */
    public void resetMetrics() {
        totalPaymentCount.set(0);
        totalSuccessCount.set(0);
        totalFailureCount.set(0);
        totalPaymentAmount.set(0);
        todayPaymentCount.set(0);
        todaySuccessCount.set(0);
        todayFailureCount.set(0);

        log.info("PaymentMetrics 초기화 완료");
    }

    /**
     * 결제 통계 DTO
     */
    @Builder
    @Getter
    public static class PaymentStatistics {
        private final long totalPaymentCount;
        private final long totalSuccessCount;
        private final long totalFailureCount;
        private final long totalPaymentAmount;
        private final double successRate;
        private final long todayPaymentCount;
        private final long todaySuccessCount;
        private final long todayFailureCount;
        private final double todaySuccessRate;

        @Override
        public String toString() {
            return String.format(
                "PaymentStatistics{총결제=%d, 총성공=%d, 총실패=%d, 총금액=%d, 성공률=%.2f%%, " +
                    "오늘결제=%d, 오늘성공=%d, 오늘실패=%d, 오늘성공률=%.2f%%}",
                totalPaymentCount, totalSuccessCount, totalFailureCount, totalPaymentAmount, successRate,
                todayPaymentCount, todaySuccessCount, todayFailureCount, todaySuccessRate
            );
        }
    }
}