package com.gov.core.Scheduler;

import com.gov.core.dto.CouponExpiryResult;
import com.gov.core.service.CouponExpiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponExpiryScheduler {

    private final CouponExpiryService couponExpiryService;

    /**
     * 매일 새벽 2시에 만료 쿠폰 처리
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul", fixedDelay = Long.MAX_VALUE)
    public void processExpiredCoupons() {
        log.info("정기 쿠폰 만료 처리 배치 시작");

        try {
            CouponExpiryResult result = couponExpiryService.processExpiredCoupons();

            if (result.hasProcessedItems()) {
                log.info("정기 쿠폰 만료 처리 완료: {}", result.getSummary());

                // 에러가 있는 경우 상세 로그 출력
                if (result.hasErrors()) {
                    log.warn("배치 처리 중 에러 발생: {}", result.getDetailedSummary());
                }

                // 성공률이 낮은 경우 경고
                if (result.getSuccessRate() < 95.0) {
                    log.warn("배치 성공률이 낮습니다: ({}개 중 {}개 성공)",
                        result.totalCount(),
                        result.successCount());
                }

            } else {
                log.info("정기 쿠폰 만료 처리 완료: 만료 대상 없음");
            }

        } catch (Exception e) {
            log.error("정기 쿠폰 만료 처리 실패", e);
        }
    }
}
