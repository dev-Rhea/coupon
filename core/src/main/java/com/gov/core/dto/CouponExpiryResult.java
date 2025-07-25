package com.gov.core.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.Builder;

@Builder
public record CouponExpiryResult(
    int totalCount,
    int successCount,
    int errorCount,
    BigDecimal totalExpiredAmount,
    List<String> errorMessages,
    LocalDateTime processedAt
) {

    /**
     * 쿠폰 만료 처리 결과 DTO
     */
    public CouponExpiryResult {
        // null 체크 및 기본값 설정
        if (totalExpiredAmount == null) {
            totalExpiredAmount = BigDecimal.ZERO;
        }
        if (errorMessages == null) {
            errorMessages = Collections.emptyList();
        }
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }

        // 유효성 검증
        if (totalCount < 0 || successCount < 0 || errorCount < 0) {
            throw new IllegalArgumentException("카운트 값은 음수일 수 없습니다");
        }
        if (successCount + errorCount > totalCount) {
            throw new IllegalArgumentException("성공 + 실패 건수가 전체 건수를 초과할 수 없습니다");
        }
        if (totalExpiredAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("만료 금액은 음수일 수 없습니다");
        }

        // 불변성을 위한 방어적 복사
        errorMessages = List.copyOf(errorMessages);
    }

    /**
     * 빈 결과 생성 (처리 대상이 없는 경우)
     */
    public static CouponExpiryResult empty() {
        return CouponExpiryResult.builder()
            .totalCount(0)
            .successCount(0)
            .errorCount(0)
            .totalExpiredAmount(BigDecimal.ZERO)
            .errorMessages(Collections.emptyList())
            .processedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 성공 결과 생성 (에러 없이 모든 처리 성공)
     */
    public static CouponExpiryResult success(int totalCount, BigDecimal totalExpiredAmount) {
        return CouponExpiryResult.builder()
            .totalCount(totalCount)
            .successCount(totalCount)
            .errorCount(0)
            .totalExpiredAmount(totalExpiredAmount)
            .errorMessages(Collections.emptyList())
            .processedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 부분 성공 결과 생성 (일부 에러 포함)
     */
    public static CouponExpiryResult partial(int totalCount, int successCount,
        BigDecimal totalExpiredAmount, List<String> errorMessages) {
        return CouponExpiryResult.builder()
            .totalCount(totalCount)
            .successCount(successCount)
            .errorCount(totalCount - successCount)
            .totalExpiredAmount(totalExpiredAmount)
            .errorMessages(errorMessages != null ? errorMessages : Collections.emptyList())
            .processedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 에러가 있는지 확인
     */
    public boolean hasErrors() {
        return errorCount > 0;
    }

    /**
     * 성공률 계산 (퍼센트)
     */
    public double getSuccessRate() {
        return totalCount > 0 ? (double) successCount / totalCount * 100.0 : 0.0;
    }

    /**
     * 실패율 계산 (퍼센트)
     */
    public double getErrorRate() {
        return totalCount > 0 ? (double) errorCount / totalCount * 100.0 : 0.0;
    }

    /**
     * 처리가 완전히 성공했는지 확인
     */
    public boolean isCompleteSuccess() {
        return totalCount > 0 && errorCount == 0;
    }

    /**
     * 처리 대상이 있었는지 확인
     */
    public boolean hasProcessedItems() {
        return totalCount > 0;
    }

    /**
     * 평균 만료 금액 계산
     */
    public BigDecimal getAverageExpiredAmount() {
        if (successCount == 0) {
            return BigDecimal.ZERO;
        }
        return totalExpiredAmount.divide(
            BigDecimal.valueOf(successCount),
            2,
            java.math.RoundingMode.HALF_UP
        );
    }

    /**
     * 요약 정보 생성
     */
    public String getSummary() {
        if (totalCount == 0) {
            return "처리 대상 없음";
        }

        return String.format(
            "총 %d개 처리 - 성공: %d개(%.1f%%), 실패: %d개(%.1f%%), 만료금액: %s원",
            totalCount,
            successCount, getSuccessRate(),
            errorCount, getErrorRate(),
            totalExpiredAmount
        );
    }

    /**
     * 상세 정보 생성 (에러 메시지 포함)
     */
    public String getDetailedSummary() {
        StringBuilder sb = new StringBuilder(getSummary());

        if (hasErrors()) {
            sb.append("\n에러 목록:");
            for (int i = 0; i < errorMessages.size() && i < 5; i++) { // 최대 5개만 표시
                sb.append("\n  ").append(i + 1).append(". ").append(errorMessages.get(i));
            }
            if (errorMessages.size() > 5) {
                sb.append("\n  ... 외 ").append(errorMessages.size() - 5).append("개 더");
            }
        }

        return sb.toString();
    }

    /**
     * JSON 형태의 문자열 표현
     */
    @Override
    public String toString() {
        return String.format(
            "CouponExpiryResult{total=%d, success=%d, error=%d, amount=%s, rate=%.1f%%, processedAt=%s}",
            totalCount, successCount, errorCount, totalExpiredAmount, getSuccessRate(), processedAt
        );
    }
}
