package com.user.register.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OTPCode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;
    private int remainingAttempts = 5; // default 5 attempts
    private String otp;
    private String type; // registration, password_reset, login
    private LocalDateTime expiresAt;
    private int attempts = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
}