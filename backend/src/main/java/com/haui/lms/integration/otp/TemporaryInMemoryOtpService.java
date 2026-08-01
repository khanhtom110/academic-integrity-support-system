package com.haui.lms.integration.otp;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.exception.extended.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class TemporaryInMemoryOtpService implements OtpService {

    private static final long TTL_MILLIS = 5 * 60 * 1000L;

    private record Entry(String otp, long expiresAtEpochMilli) {
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public String generateAndSendOtp(String email, OtpPurpose purpose) {
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        store.put(key(email, purpose), new Entry(otp, Instant.now().toEpochMilli() + TTL_MILLIS));
        log.info("[TEMP OTP - chua gui mail that] email={} purpose={} otp={}", email, purpose, otp);
        return otp;
    }

    @Override
    public void verifyOtp(String email, String otp, OtpPurpose purpose) {
        Entry entry = store.get(key(email, purpose));
        if (entry == null || Instant.now().toEpochMilli() > entry.expiresAtEpochMilli()) {
            throw new AppException(400, ErrorMessage.Auth.OTP_EXPIRED);
        }
        if (!entry.otp().equals(otp)) {
            throw new AppException(400, ErrorMessage.Auth.INVALID_OTP);
        }
        store.remove(key(email, purpose));
    }

    private String key(String email, OtpPurpose purpose) {
        return "OTP:" + email;
    }
}
