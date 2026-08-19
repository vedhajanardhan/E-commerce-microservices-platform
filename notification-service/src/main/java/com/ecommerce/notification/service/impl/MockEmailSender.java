package com.ecommerce.notification.service.impl;

import com.ecommerce.notification.service.EmailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stands in for a real email provider (SendGrid, SES, etc.). Logging the
 * "sent" email is enough to demonstrate the notification flow end to end
 * without needing real SMTP credentials or an external dependency —
 * swapping this for a real implementation later is a one-class change
 * since everything else depends on the {@link EmailSender} interface.
 */
@Slf4j
@Component
public class MockEmailSender implements EmailSender {

    @Override
    public boolean send(String toEmail, String subject, String body) {
        log.info("========== MOCK EMAIL ==========");
        log.info("To: {}", toEmail);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);
        log.info("=================================");
        return true;
    }
}
