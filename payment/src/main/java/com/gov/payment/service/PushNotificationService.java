package com.gov.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushNotificationService {

    public void sendToUser(String userId, String title, String message) {
        log.info("푸시 알림 발송: userId={}, title={}", userId, title);

        try {
            // 사용자 디바이스 토큰 조회
            String deviceToken = getUserDeviceToken(userId);

            // 푸시 알림 발송 (Mock)
            sendPushNotification(deviceToken, title, message);

        } catch (Exception e) {
            log.error("푸시 알림 발송 실패: userId={}", userId, e);
        }
    }

    public void sendToAdmins(String title, String message) {
        log.info("관리자 푸시 알림 발송: title={}", title);

        try {
            // 관리자 목록 조회
            String[] adminTokens = getAdminDeviceTokens();

            // 모든 관리자에게 발송
            for (String token : adminTokens) {
                sendPushNotification(token, title, message);
            }

        } catch (Exception e) {
            log.error("관리자 푸시 알림 발송 실패", e);
        }
    }

    private String getUserDeviceToken(String userId) {
        // 실제로는 DB에서 조회
        return "user_device_token_" + userId;
    }

    private String[] getAdminDeviceTokens() {
        // 실제로는 DB에서 조회
        return new String[]{"admin_token_1", "admin_token_2"};
    }

    private void sendPushNotification(String deviceToken, String title, String message) {
        // Mock 푸시 알림 발송 (Firebase, APNs 등 사용)
        log.debug("Mock 푸시 알림 발송: token={}, title={}", deviceToken, title);
    }

}
