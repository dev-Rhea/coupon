package com.gov.payment.service;

import com.gov.payment.entity.Payment;
import com.gov.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotificationService {

    private final PaymentRepository paymentRepository;
    private final PushNotificationService pushNotificationService;
    private final SmsService smsService;
    private final EmailService emailService;

    @Value("${payment.notification.large-amount-threshold:100000}")
    private BigDecimal largeAmountThreshold;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 결제 성공 알림 발송
     */
    @Async
    public CompletableFuture<Void> sendPaymentSuccessNotification(String paymentId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));

                log.info("결제 성공 알림 발송 시작: paymentId={}", paymentId);

                CompletableFuture.allOf(
                    sendUserSuccessNotification(payment),
                    sendMerchantSuccessNotification(payment),
                    sendAdminSuccessNotification(payment)
                ).join();

                log.info("결제 성공 알림 발송 완료: paymentId={}", paymentId);

            } catch (Exception e) {
                log.error("결제 성공 알림 발송 실패: paymentId={}", paymentId, e);
                throw new RuntimeException("알림 발송 실패", e);
            }
        });
    }

    /**
     * 결제 실패 알림 발송
     */
    @Async
    public CompletableFuture<Void> sendPaymentFailureNotification(String paymentId, String errorReason) {
        return CompletableFuture.runAsync(() -> {
            try {
                Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다: " + paymentId));

                log.info("결제 실패 알림 발송 시작: paymentId={}, reason={}", paymentId, errorReason);

                CompletableFuture.allOf(
                    sendUserFailureNotification(payment, errorReason),
                    sendMerchantFailureNotification(payment, errorReason),
                    sendAdminFailureNotification(payment, errorReason)
                ).join();

                log.info("결제 실패 알림 발송 완료: paymentId={}", paymentId);

            } catch (Exception e) {
                log.error("결제 실패 알림 발송 실패: paymentId={}", paymentId, e);
                throw new RuntimeException("알림 발송 실패", e);
            }
        });
    }

    /**
     * 사용자 성공 알림
     */
    private CompletableFuture<Void> sendUserSuccessNotification(Payment payment) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 푸시 알림
                String pushMessage = String.format("결제가 완료되었습니다. 금액: %,d원",
                    payment.getAmount().intValue());
                pushNotificationService.sendToUser(payment.getUser().getUserId(), "결제 완료", pushMessage);

                // SMS 알림 (선택적)
                if (payment.getUser().getPhone() != null) {
                    String smsMessage = String.format("[정부쿠폰] 결제완료 %,d원 (ID:%s)",
                        payment.getAmount().intValue(),
                        payment.getPaymentId().substring(0, Math.min(8, payment.getPaymentId().length())));
                    smsService.sendSms(payment.getUser().getPhone(), smsMessage);
                }

                log.debug("사용자 성공 알림 발송 완료: userId={}", payment.getUser().getUserId());

            } catch (Exception e) {
                log.error("사용자 성공 알림 발송 실패: userId={}", payment.getUser().getUserId(), e);
            }
        });
    }

    /**
     * 가맹점 성공 알림
     */
    private CompletableFuture<Void> sendMerchantSuccessNotification(Payment payment) {
        return CompletableFuture.runAsync(() -> {
            try {
                String emailSubject = "[정부쿠폰] 결제 승인 알림";
                String emailBody = String.format("""
                    안녕하세요,
                    
                    다음 결제가 승인되었습니다:
                    - 결제 ID: %s
                    - 결제 금액: %,d원
                    - 결제 시간: %s
                    - PG 거래번호: %s
                    
                    감사합니다.
                    """,
                    payment.getPaymentId(),
                    payment.getAmount().intValue(),
                    payment.getPaymentDate() != null ?
                        payment.getPaymentDate().format(DATE_FORMATTER) : "N/A",
                    payment.getPgTransactionId() != null ?
                        payment.getPgTransactionId() : "N/A");

                emailService.sendToMerchant(payment.getMerchant().getMerchantId(), emailSubject, emailBody);

                log.debug("가맹점 성공 알림 발송 완료: merchantId={}", payment.getMerchant().getMerchantId());

            } catch (Exception e) {
                log.error("가맹점 성공 알림 발송 실패: merchantId={}", payment.getMerchant().getMerchantId(), e);
            }
        });
    }

    /**
     * 관리자 성공 알림 (대용량 거래 시에만)
     */
    private CompletableFuture<Void> sendAdminSuccessNotification(Payment payment) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 대용량 거래만 관리자에게 알림
                if (payment.getAmount().compareTo(largeAmountThreshold) >= 0) {
                    String adminMessage = String.format("대용량 결제 완료: %s (%,d원)",
                        payment.getPaymentId(),
                        payment.getAmount().intValue());
                    pushNotificationService.sendToAdmins("대용량 결제 알림", adminMessage);

                    log.debug("관리자 대용량 결제 알림 발송: paymentId={}", payment.getPaymentId());
                }
            } catch (Exception e) {
                log.error("관리자 성공 알림 발송 실패: paymentId={}", payment.getPaymentId(), e);
            }
        });
    }

    /**
     * 사용자 실패 알림
     */
    private CompletableFuture<Void> sendUserFailureNotification(Payment payment, String errorReason) {
        return CompletableFuture.runAsync(() -> {
            try {
                String userFriendlyReason = convertToUserFriendlyMessage(errorReason);

                // 푸시 알림
                pushNotificationService.sendToUser(payment.getUser().getUserId(), "결제 실패", userFriendlyReason);

                log.debug("사용자 실패 알림 발송 완료: userId={}", payment.getUser().getUserId());

            } catch (Exception e) {
                log.error("사용자 실패 알림 발송 실패: userId={}", payment.getUser().getUserId(), e);
            }
        });
    }

    /**
     * 가맹점 실패 알림
     */
    private CompletableFuture<Void> sendMerchantFailureNotification(Payment payment, String errorReason) {
        return CompletableFuture.runAsync(() -> {
            try {
                String emailSubject = "[정부쿠폰] 결제 실패 알림";
                String emailBody = String.format("""
                    안녕하세요,
                    
                    다음 결제가 실패하였습니다:
                    - 결제 ID: %s
                    - 실패 사유: %s
                    - 시도 시간: %s
                    
                    고객에게 안내 부탁드립니다.
                    """,
                    payment.getPaymentId(),
                    errorReason != null ? errorReason : "알 수 없는 오류",
                    payment.getCreatedAt() != null ?
                        payment.getCreatedAt().format(DATE_FORMATTER) : "N/A");

                emailService.sendToMerchant(payment.getMerchant().getMerchantId(), emailSubject, emailBody);

                log.debug("가맹점 실패 알림 발송 완료: merchantId={}", payment.getMerchant().getMerchantId());

            } catch (Exception e) {
                log.error("가맹점 실패 알림 발송 실패: merchantId={}", payment.getMerchant().getMerchantId(), e);
            }
        });
    }

    /**
     * 관리자 실패 알림
     */
    private CompletableFuture<Void> sendAdminFailureNotification(Payment payment, String errorReason) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 시스템 오류인 경우만 관리자에게 알림
                if (isSystemError(errorReason)) {
                    String adminMessage = String.format("시스템 오류 발생: %s - %s",
                        payment.getPaymentId(),
                        errorReason != null ? errorReason : "알 수 없는 시스템 오류");
                    pushNotificationService.sendToAdmins("시스템 오류 알림", adminMessage);

                    log.debug("관리자 시스템 오류 알림 발송: paymentId={}", payment.getPaymentId());
                }
            } catch (Exception e) {
                log.error("관리자 실패 알림 발송 실패: paymentId={}", payment.getPaymentId(), e);
            }
        });
    }

    /**
     * 사용자 친화적 메시지로 변환
     */
    private String convertToUserFriendlyMessage(String errorReason) {
        if (errorReason == null || errorReason.trim().isEmpty()) {
            return "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }

        return switch (errorReason.toLowerCase()) {
            case "쿠폰 잔액이 부족합니다", "insufficient balance" ->
                "쿠폰 잔액이 부족합니다. 잔액을 확인해주세요.";
            case "pg 승인 실패", "payment gateway failure", "card declined" ->
                "카드 승인이 거절되었습니다. 다른 카드로 시도해주세요.";
            case "pg 통신 오류", "communication error", "network error" ->
                "일시적인 통신 오류입니다. 잠시 후 다시 시도해주세요.";
            case "timeout", "연결 시간 초과" ->
                "처리 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.";
            default -> "결제 처리 중 오류가 발생했습니다. 고객센터로 문의해주세요.";
        };
    }

    /**
     * 시스템 오류인지 판단
     */
    private boolean isSystemError(String errorReason) {
        if (errorReason == null || errorReason.trim().isEmpty()) {
            return true;
        }

        String lowerErrorReason = errorReason.toLowerCase();
        return lowerErrorReason.contains("통신 오류") ||
            lowerErrorReason.contains("시스템") ||
            lowerErrorReason.contains("내부 오류") ||
            lowerErrorReason.contains("데이터베이스") ||
            lowerErrorReason.contains("network error") ||
            lowerErrorReason.contains("system error") ||
            lowerErrorReason.contains("internal error") ||
            lowerErrorReason.contains("database") ||
            lowerErrorReason.contains("timeout") ||
            lowerErrorReason.contains("connection");
    }

}
