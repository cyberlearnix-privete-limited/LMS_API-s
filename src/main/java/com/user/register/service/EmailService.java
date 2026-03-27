package com.user.register.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Generate 6-digit OTP
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Send OTP Email
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp);
            message.setFrom("mallibhai7876@gmail.com"); // must match your SMTP username

            mailSender.send(message);
            System.out.println("OTP sent successfully to " + toEmail);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending OTP: " + e.getMessage());
        }
    }
}