package com.gov.settlement.scheduler;

import com.gov.settlement.service.SettlementService;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SettlementScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SettlementScheduler.class);

    private SettlementService settlementService;

    /**
     * 매일 새벽 2시에 전날 정산 처리
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul", fixedDelay = Long.MAX_VALUE)
    public void dailySettlement() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        try {
            logger.info("자동 정산 시작: {}", yesterday);
            settlementService.processDailySettlement(yesterday);
            logger.info("자동 정산 완료: {}", yesterday);
        } catch (Exception e) {
            logger.error("자동 정산 실패: {}", yesterday, e);
        }
    }
}
