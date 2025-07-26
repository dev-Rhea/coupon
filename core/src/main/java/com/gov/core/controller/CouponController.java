package com.gov.core.controller;

import com.gov.core.dto.CouponResDto;
import com.gov.core.dto.CouponUseReqDto;
import com.gov.core.dto.CouponValidationResDto;
import com.gov.core.entity.Coupon;
import com.gov.core.service.CouponService;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /**
     * 사용자의 활성 쿠폰 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<CouponResDto>> getCoupons(
        @RequestParam String userId) {

        List<Coupon> coupons = couponService.getActiveCoupons(userId);
        List<CouponResDto> response = coupons.stream()
            .map(CouponResDto.Response::from)
            .collect(Collectors.toList());

        log.info("쿠폰 목록 조회 API 호출: userId={}, count={}", userId, response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 쿠폰 상세 조회
     */
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponResDto> getCoupon(
        @PathVariable String couponId,
        @RequestParam String userId) {

        Coupon coupon = couponService.getCoupon(couponId, userId);
        CouponResDto response = CouponResDto.Response.from(coupon);

        log.info("쿠폰 상세 조회 API 호출: couponId={}, userId={}", couponId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 금액 사용 가능한 쿠폰 조회
     */
    @GetMapping("/usable")
    public ResponseEntity<List<CouponResDto>> getUsableCoupons(
        @RequestParam String userId,
        @RequestParam BigDecimal amount) {

        List<Coupon> coupons = couponService.getUsableCoupons(userId, amount);
        List<CouponResDto> response = coupons.stream()
            .map(CouponResDto.Response::from)
            .collect(Collectors.toList());

        log.info("사용 가능한 쿠폰 조회 API 호출: userId={}, amount={}, count={}",
            userId, amount, response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 쿠폰 사용 검증
     */
    @PostMapping("/{couponId}/validate")
    public ResponseEntity<CouponValidationResDto> validateCoupon(
        @PathVariable String couponId,
        @RequestBody CouponUseReqDto request) {

        boolean isValid = couponService.validateCouponUsage(
            couponId, request.userId(), request.amount());

        if (isValid) {
            Coupon coupon = couponService.getCoupon(couponId, request.userId());
            CouponValidationResDto response = CouponValidationResDto.success(
                coupon.getRemainingAmount());

            log.info("쿠폰 검증 성공: couponId={}, userId={}, amount={}",
                couponId, request.userId(), request.amount());
            return ResponseEntity.ok(response);
        } else {
            CouponValidationResDto response = CouponValidationResDto.failure(
                "쿠폰을 사용할 수 없습니다.");

            log.warn("쿠폰 검증 실패: couponId={}, userId={}, amount={}",
                couponId, request.userId(), request.amount());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 쿠폰 금액 예약 (결제 시작)
     */
    @PostMapping("/{couponId}/reserve")
    public ResponseEntity<String> reserveCoupon(
        @PathVariable String couponId,
        @RequestBody CouponUseReqDto request) {

        boolean reserved = couponService.reserveCouponAmount(
            couponId, request.userId(), request.amount());

        if (reserved) {
            log.info("쿠폰 예약 성공: couponId={}, userId={}, amount={}",
                couponId, request.userId(), request.amount());
            return ResponseEntity.ok("쿠폰 예약이 완료되었습니다.");
        } else {
            log.warn("쿠폰 예약 실패: couponId={}, userId={}, amount={}",
                couponId, request.userId(), request.amount());
            return ResponseEntity.badRequest().body("쿠폰 예약에 실패했습니다.");
        }
    }

    /**
     * 쿠폰 사용 확정 (결제 완료)
     */
    @PostMapping("/{couponId}/confirm")
    public ResponseEntity<String> confirmCoupon(
        @PathVariable String couponId,
        @RequestBody CouponUseReqDto request) {

        try {
            couponService.confirmCouponUsage(
                couponId, request.userId(), request.amount());

            log.info("쿠폰 사용 확정 성공: couponId={}, userId={}, amount={}",
                couponId, request.userId(), request.amount());
            return ResponseEntity.ok("쿠폰 사용이 확정되었습니다.");

        } catch (Exception e) {
            log.error("쿠폰 사용 확정 실패: couponId={}, userId={}", couponId, request.userId(), e);
            return ResponseEntity.badRequest().body("쿠폰 사용 확정에 실패했습니다.");
        }
    }

    /**
     * 쿠폰 사용 취소 (결제 실패)
     */
    @PostMapping("/{couponId}/cancel")
    public ResponseEntity<String> cancelCoupon(
        @PathVariable String couponId,
        @RequestBody CouponUseReqDto request) {

        try {
            couponService.cancelCouponUsage(
                couponId, request.userId(), request.amount());

            log.info("쿠폰 사용 취소 성공: couponId={}, userId={}, amount={}",
                couponId, request.userId(), request.amount());
            return ResponseEntity.ok("쿠폰 사용이 취소되었습니다.");

        } catch (Exception e) {
            log.error("쿠폰 사용 취소 실패: couponId={}, userId={}", couponId, request.userId(), e);
            return ResponseEntity.badRequest().body("쿠폰 사용 취소에 실패했습니다.");
        }
    }

    /**
     * 사용자 총 쿠폰 잔액 조회
     */
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getTotalBalance(@RequestParam String userId) {
        BigDecimal totalBalance = couponService.getTotalBalance(userId);

        log.info("총 쿠폰 잔액 조회: userId={}, balance={}", userId, totalBalance);
        return ResponseEntity.ok(totalBalance);
    }

    /**
     * 쿠폰 잔액 동기화 (관리용)
     */
    @PostMapping("/{couponId}/sync")
    public ResponseEntity<String> syncBalance(@PathVariable String couponId) {
        try {
            couponService.syncCouponBalance(couponId);

            log.info("쿠폰 잔액 동기화 성공: couponId={}", couponId);
            return ResponseEntity.ok("쿠폰 잔액 동기화가 완료되었습니다.");

        } catch (Exception e) {
            log.error("쿠폰 잔액 동기화 실패: couponId={}", couponId, e);
            return ResponseEntity.badRequest().body("쿠폰 잔액 동기화에 실패했습니다.");
        }
    }

}
