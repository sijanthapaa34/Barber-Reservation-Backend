package com.sijan.barberReservation.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    // In-memory store: Email -> {OTP, ExpiryTime}
    private final Map<String, OtpEntry> otpStore = new HashMap<>();
    private static final int OTP_LENGTH = 6;
    private static final long OTP_VALID_DURATION = TimeUnit.MINUTES.toMillis(5); // 5 minutes validity

    private static class OtpEntry {
        String otp;
        long expiryTime;

        OtpEntry(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    public String generateOtp(String email) {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }

        String otpStr = otp.toString();
        otpStore.put(email, new OtpEntry(otpStr, System.currentTimeMillis() + OTP_VALID_DURATION));
        return otpStr;
    }

    public boolean verifyOtp(String email, String otpInput) {
        OtpEntry entry = otpStore.get(email);

        if (entry == null) {
            return false; // OTP not found
        }

        // Check if expired
        if (System.currentTimeMillis() > entry.expiryTime) {
            otpStore.remove(email);
            return false;
        }

        // Check if matches
        if (entry.otp.equals(otpInput)) {
            otpStore.remove(email); // Invalidate OTP after use
            return true;
        }

        return false;
    }
}
