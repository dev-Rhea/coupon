package com.gov.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendToMerchant(String merchantId, String subject, String body) {
        // 실제 이메일 발송 로직
        log.info("이메일 발송: merchantId={}, subject={}", merchantId, subject);

        // Mock 구현 - 실제로는 SMTP 또는 이메일 서비스 API 사용
        try {
            // 가맹점 이메일 주소 조회
            String merchantEmail = getMerchantEmail(merchantId);

            // 이메일 발송 (Mock)
            sendEmail(merchantEmail, subject, body);

        } catch (Exception e) {
            log.error("이메일 발송 실패: merchantId={}", merchantId, e);
        }
    }

    private String getMerchantEmail(String merchantId) {
        // 실제로는 DB에서 조회
        return merchantId + "@merchant.example.com";
    }

    private void sendEmail(String to, String subject, String body) {
        // Mock 이메일 발송
        log.debug("Mock 이메일 발송: to={}, subject={}", to, subject);
    }

}
