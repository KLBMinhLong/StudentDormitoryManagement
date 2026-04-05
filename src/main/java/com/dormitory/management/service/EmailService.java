package com.dormitory.management.service;

public interface EmailService {

    boolean sendResetPasswordEmail(String email, String resetLink);

    boolean sendNotificationEmail(String email, String subject, String message);
}
