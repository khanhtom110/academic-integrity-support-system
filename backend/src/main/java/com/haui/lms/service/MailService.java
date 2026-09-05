package com.haui.lms.service;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;

public interface MailService {
    void sendOtp(String email, String otp);
}
