package com.gov.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    public void sendSms(String userId, String message) {
        log.info("SMS 발송: userId={}, message={}", userId, message);

        try {
            // 사용자 전화번호 조회
            String phoneNumber = getUserPhoneNumber(userId);

            // SMS 발송 (Mock)
            sendSmsMessage(phoneNumber, message);

        } catch (Exception e) {
            log.error("SMS 발송 실패: userId={}", userId, e);
        }
    }

    private String getUserPhoneNumber(String userId) {
        // 실제로는 DB에서 조회
        return "010-1234-5678";
    }

    private void sendSmsMessage(String phoneNumber, String message) {
        // Mock SMS 발송
        log.debug("Mock SMS 발송: to={}, message={}", phoneNumber, message);
    }

}
