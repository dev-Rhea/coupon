package com.gov.payment.service;

import com.gov.payment.entity.Payment;
import com.gov.payment.repository.PaymentRepository;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final PaymentRepository paymentRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushNotificationService;

    public void sendPaymentSuccessNotification(String paymentId) {
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
        }
    }

    public void sendPaymentFailureNotification(String paymentId, String errorReason) {
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
        }
    }

    private CompletableFuture<Void> sendUserSuccessNotification(Payment payment) {
        return CompletableFuture.runAsync(() -> {
            try {
                String pushMessage = String.format("결제가 완료되었습니다. 금액: %,d원",
                    payment.getAmount().intValue());
                pushNotificationService.sendToUser(payment.getUser().getUserId(), "결제 완료", pushMessage);

                String smsMessage = String.format("[정부쿠폰] 결제완료 %,d원 (ID:%s)",
                    payment.getAmount().intValue(),
                    payment.getPaymentId().substring(0, 8));
                smsService.sendSms(payment.getUser().getUserId(), smsMessage);

                log.debug("사용자 성공 알림 발송 완료: userId={}", payment.getUser().getUserId());

            } catch (Exception e) {
                log.error("사용자 성공 알림 발송 실패: userId={}", payment.getUser().getUserId(), e);
            }
        });
    }

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
                    payment.getPaymentDate(),
                    payment.getPgTransactionId());

                emailService.sendToMerchant(payment.getMerchant().getMerchantId(), emailSubject, emailBody);

                log.debug("가맹점 성공 알림 발송 완료: merchantId={}", payment.getMerchant().getMerchantId());

            } catch (Exception e) {
                log.error("가맹점 성공 알림 발송 실패: merchantId={}", payment.getMerchant().getMerchantId(), e);
            }
        });
    }

    private CompletableFuture<Void> sendAdminSuccessNotification(Payment payment) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (payment.getAmount().intValue() >= 100000) {
                    String adminMessage = String.format("대용량 결제 완료: %s (%,d원)",
                        payment.getPaymentId(),
                        payment.getAmount().intValue());
                    pushNotificationService.sendToAdmins("대용량 결제 알림", adminMessage);

                    log.debug("관리자 대용량 결제 알림 발송: paymentId={}", payment.getPaymentId());
                }
            } catch (Exception e) {
                log.error("관리자 알림 발송 실패: paymentId={}", payment.getPaymentId(), e);
            }
        });
    }

    private CompletableFuture<Void> sendUserFailureNotification(Payment payment, String errorReason) {
        return CompletableFuture.runAsync(() -> {
            try {
                String userFriendlyReason = convertToUserFriendlyMessage(errorReason);
                pushNotificationService.sendToUser(payment.getUser().getUserId(), "결제 실패", userFriendlyReason);

                log.debug("사용자 실패 알림 발송 완료: userId={}", payment.getUser().getUserId());

            } catch (Exception e) {
                log.error("사용자 실패 알림 발송 실패: userId={}", payment.getUser().getUserId(), e);
            }
        });
    }

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
                    errorReason,
                    payment.getCreatedAt());

                emailService.sendToMerchant(payment.getMerchant().getMerchantId(), emailSubject, emailBody);

                log.debug("가맹점 실패 알림 발송 완료: merchantId={}", payment.getMerchant().getMerchantId());

            } catch (Exception e) {
                log.error("가맹점 실패 알림 발송 실패: merchantId={}", payment.getMerchant().getMerchantId(), e);
            }
        });
    }

    private CompletableFuture<Void> sendAdminFailureNotification(Payment payment, String errorReason) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (isSystemError(errorReason)) {
                    String adminMessage = String.format("시스템 오류 발생: %s - %s",
                        payment.getPaymentId(), errorReason);
                    pushNotificationService.sendToAdmins("시스템 오류 알림", adminMessage);

                    log.debug("관리자 시스템 오류 알림 발송: paymentId={}", payment.getPaymentId());
                }
            } catch (Exception e) {
                log.error("관리자 실패 알림 발송 실패: paymentId={}", payment.getPaymentId(), e);
            }
        });
    }

    private String convertToUserFriendlyMessage(String errorReason) {
        if (errorReason == null) {
            return "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }

        return switch (errorReason) {
            case "쿠폰 잔액이 부족합니다" -> "쿠폰 잔액이 부족합니다. 잔액을 확인해주세요.";
            case "PG 승인 실패" -> "카드 승인이 거절되었습니다. 다른 카드로 시도해주세요.";
            case "PG 통신 오류" -> "일시적인 통신 오류입니다. 잠시 후 다시 시도해주세요.";
            default -> "결제 처리 중 오류가 발생했습니다. 고객센터로 문의해주세요.";
        };
    }

    private boolean isSystemError(String errorReason) {
        if (errorReason == null) return true;

        return errorReason.contains("통신 오류") ||
            errorReason.contains("시스템") ||
            errorReason.contains("내부 오류") ||
            errorReason.contains("데이터베이스");
    }

}
