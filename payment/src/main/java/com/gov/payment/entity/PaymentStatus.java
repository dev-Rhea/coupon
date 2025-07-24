package com.gov.payment.entity;

public enum PaymentStatus {
    PENDING,     // 결제 대기
    PROCESSING,  // 결제 처리 중
    COMPLETED,   // 결제 완료
    FAILED,      // 결제 실패
    CANCELLED,   // 결제 취소
    REFUNDED     // 환불 완료
}
