package com.gov.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {
    PENDING("대기중", "정산 승인 대기"),
    APPROVED("승인됨", "정산 승인 완료"),
    COMPLETED("완료됨", "정산 이체 완료"),
    REJECTED("거절됨", "정산 승인 거절");

    private final String displayName;
    private final String description;
}
