package com.ecommerce.notification.service;

public interface EmailSender {

    /** @return true if the (mock) send succeeded */
    boolean send(String toEmail, String subject, String body);
}
