package com.dormitory.management.service;

public interface EmailService {

    void sendResetPasswordEmail(String email, String resetLink);

    void sendNotificationEmail(String email, String subject, String message);
}
