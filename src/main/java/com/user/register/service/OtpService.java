package com.user.register.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    public String generateOtp(String email) {

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        String hashedOtp = sha256(otp);

        redisTemplate.opsForValue().set(
                "OTP:REGISTER:" + email,
                hashedOtp,
                Duration.ofMinutes(5)
        );

        return otp; // Send via email
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}