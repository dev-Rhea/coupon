package com.gov.core.service;

import com.gov.core.dto.CouponExpiryResult;
import com.gov.core.entity.BatchJobLog;
import com.gov.core.entity.Coupon;
import com.gov.core.repository.CouponRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CouponExpiryService {

    private final CouponRepository couponRepository;
    private final CouponBalanceService balanceService;
    private final BatchJobLogService batchJobLogService;

    /**
     * 만료된 쿠폰 처리 (메인 배치 로직)
     */
    @Transactional
    public CouponExpiryResult processExpiredCoupons() {
        String jobId = UUID.randomUUID().toString();
        LocalDate currentDate = LocalDate.now();

        log.info("쿠폰 만료 처리 배치 시작: jobId={}, date={}", jobId, currentDate);

        // 배치 작업 로그 시작
        BatchJobLog batchLog = batchJobLogService.startJob(
            "COUPON_EXPIRY",
            "쿠폰 만료 처리",
            Map.of("targetDate", currentDate.toString())
        );

        try {
            // 1. 만료된 쿠폰 조회
            List<Coupon> expiredCoupons = couponRepository.findExpiredCoupons(currentDate);

            if (expiredCoupons.isEmpty()) {
                log.info("만료된 쿠폰이 없습니다.");
                batchJobLogService.completeJob(batchLog.getLogId(), 0, 0, 0);
                return CouponExpiryResult.empty();
            }

            log.info("만료 대상 쿠폰 수: {}", expiredCoupons.size());

            // 2. 만료 처리 실행
            CouponExpiryResult result = expireCoupons(expiredCoupons);

            // 3. 배치 작업 로그 완료
            batchJobLogService.completeJob(
                batchLog.getLogId(),
                result.totalCount(),        // Record의 accessor 메서드 사용
                result.successCount(),      // Record의 accessor 메서드 사용
                result.errorCount()         // Record의 accessor 메서드 사용
            );

            // 4. 통계 로깅
            logExpiryStatistics(result, currentDate);

            log.info("쿠폰 만료 처리 배치 완료: jobId={}, result={}", jobId, result.getSummary());

            return result;

        } catch (Exception e) {
            log.error("쿠폰 만료 처리 배치 실패: jobId={}", jobId, e);
            batchJobLogService.failJob(batchLog.getLogId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 쿠폰 만료 처리 실행
     */
    private CouponExpiryResult expireCoupons(List<Coupon> expiredCoupons) {
        int successCount = 0;
        int errorCount = 0;
        BigDecimal totalExpiredAmount = BigDecimal.ZERO;
        List<String> errorMessages = new ArrayList<>();

        for (Coupon coupon : expiredCoupons) {
            try {
                BigDecimal expiredAmount = coupon.getRemainingAmount();

                // 쿠폰 만료 처리 (잔액 0원으로 설정)
                coupon.expire(); // forceExpire -> expire로 수정 (메서드가 있다면)

                // Redis 캐시 삭제
                balanceService.clearBalance(coupon.getCouponId());

                totalExpiredAmount = totalExpiredAmount.add(expiredAmount);
                successCount++;

                log.debug("쿠폰 만료 처리 완료: couponId={}, expiredAmount={}",
                    coupon.getCouponId(), expiredAmount);

            } catch (Exception e) {
                errorCount++;
                String errorMsg = String.format("쿠폰 만료 처리 실패: couponId=%s, error=%s",
                    coupon.getCouponId(), e.getMessage());
                errorMessages.add(errorMsg);
                log.error(errorMsg, e);
            }
        }

        // 변경사항 일괄 저장
        if (successCount > 0) {
            couponRepository.saveAll(expiredCoupons);
        }

        // Record 생성 방법 수정 - builder() 사용
        return new CouponExpiryResult(
            successCount + errorCount,
            successCount,
            errorCount,
            totalExpiredAmount,
            errorMessages,
            LocalDateTime.now()
        );
    }

    /**
     * 만료 통계 로깅 - Record accessor 메서드 사용
     */
    private void logExpiryStatistics(CouponExpiryResult result, LocalDate targetDate) {
        if (result.successCount() > 0) {
            log.info("=== 쿠폰 만료 처리 통계 ===");
            log.info("처리 일자: {}", targetDate);
            log.info("총 처리 건수: {}", result.totalCount());
            log.info("성공 건수: {}", result.successCount());
            log.info("실패 건수: {}", result.errorCount());
            log.info("총 만료 금액: {}원", result.totalExpiredAmount());
            log.info("성공률: {}%", String.format("%.1f", result.getSuccessRate()));
            log.info("처리 완료 시간: {}", result.processedAt());

            if (result.hasErrors()) {
                log.warn("만료 처리 오류 목록:");
                result.errorMessages().forEach(log::warn);
            }
        }
    }


}
