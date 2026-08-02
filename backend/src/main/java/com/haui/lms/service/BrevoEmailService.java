package com.haui.lms.service;

public interface BrevoEmailService {
    public void sendEmail(String toEmail, String subject, String htmlContent);
}
