package com.haui.lms.integration.otp;

public interface OtpService {

    String generateAndSendOtp(String email, OtpPurpose purpose);

    void verifyOtp(String email, String otp, OtpPurpose purpose);
}
