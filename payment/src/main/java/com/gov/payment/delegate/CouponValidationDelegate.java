package com.gov.payment.delegate;

import com.gov.payment.service.CouponBalanceService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("couponValidationDelegate")
@RequiredArgsConstructor
@Slf4j
public class CouponValidationDelegate implements JavaDelegate{

    private final CouponBalanceService couponBalanceService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String couponId = (String) execution.getVariable("couponId");
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");

        log.info("쿠폰 검증 시작: couponId={}, amount={}", couponId, amount);

        // 쿠폰 잔액 예약 시도
        boolean reserved = couponBalanceService.reserveAmount(couponId, amount);

        execution.setVariable("couponReserved", reserved);

        if (!reserved) {
            execution.setVariable("validationError", "쿠폰 잔액이 부족합니다");
            log.warn("쿠폰 검증 실패: couponId={}, amount={}", couponId, amount);
        } else {
            log.info("쿠폰 검증 성공: couponId={}, amount={}", couponId, amount);
        }
    }
}
