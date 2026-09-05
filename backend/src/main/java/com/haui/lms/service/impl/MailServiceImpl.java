package com.haui.lms.service.impl;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.service.BrevoEmailService;
import com.haui.lms.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final BrevoEmailService brevoEmailService;

    @Override
    public void sendOtp(String email, String otp) {
        try {
            String subject = "Mã xác nhận Hệ thống Liêm chính học thuật";
            String htmlContent = "<h3>Xin chào,</h3>"
                    + "<p>Mã OTP của bạn là: <strong style='font-size: 24px; color: blue;'>" + otp + "</strong></p>"
                    + "<p>Mã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>";
            brevoEmailService.sendEmail(email, subject, htmlContent);
        } catch (Exception e) {
            System.err.println("Failure when sending mail for: " + email + " - Exception: " + e.getMessage());
            throw new AppException(500, ErrorMessage.Auth.SEND_MAIL_FAIL);
        }
    }
}
