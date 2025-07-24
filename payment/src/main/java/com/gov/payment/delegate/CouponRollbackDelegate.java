package com.gov.payment.delegate;

import com.gov.payment.service.CouponBalanceService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("couponRollbackDelegate")
@RequiredArgsConstructor
@Slf4j
public class CouponRollbackDelegate implements JavaDelegate {

    private final CouponBalanceService couponBalanceService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String couponId = (String) execution.getVariable("couponId");
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        Boolean couponReserved = (Boolean) execution.getVariable("couponReserved");

        log.info("쿠폰 롤백 시작: couponId={}, amount={}, couponReserved={}",
            couponId, amount, couponReserved);

        // 쿠폰이 예약되었다면 롤백
        if (Boolean.TRUE.equals(couponReserved)) {
            couponBalanceService.restoreAmount(couponId, amount);
            log.info("쿠폰 롤백 완료: couponId={}, amount={}", couponId, amount);
        } else {
            log.info("쿠폰 롤백 불필요: couponId={}, couponReserved={}", couponId, couponReserved);
        }
    }
}
