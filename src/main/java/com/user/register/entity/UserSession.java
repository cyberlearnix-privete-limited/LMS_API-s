package com.user.register.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String deviceInfo;   // optional: browser/device info
    @Column(columnDefinition = "TEXT")
    private String token;  // if needed
    @Column(columnDefinition = "TEXT")
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private String ipAddress;
    private LocalDateTime expiresAt;
}