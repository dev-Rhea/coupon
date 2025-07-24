package com.gov.payment.delegate;

import com.gov.payment.service.CouponBalanceService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("couponConfirmDelegate")
@RequiredArgsConstructor
@Slf4j
public class CouponConfirmDelegate implements JavaDelegate {

    private final CouponBalanceService couponBalanceService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String couponId = (String) execution.getVariable("couponId");
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");

        log.info("쿠폰 사용 확정 시작: couponId={}, amount={}", couponId, amount);

        // 쿠폰 사용 확정
        couponBalanceService.confirmUsage(couponId, amount);

        execution.setVariable("couponConfirmed", true);
        log.info("쿠폰 사용 확정 완료: couponId={}, amount={}", couponId, amount);
    }

}
