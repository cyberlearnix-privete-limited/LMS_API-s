package com.user.register.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LogoutResponse {

    // For single device logout
    private Long userId;
    private String email;
    private String logoutDevice;
    private String logoutIp;
    private LocalDateTime logoutTime;

    // For logout all sessions
    private int totalSessionsRevoked;
    private List<SessionDto> revokedSessions;  // SessionDto should include id, deviceInfo, IP, createdAt, email
    private LocalDateTime timestamp;

    // Single device constructor
    public LogoutResponse(Long userId, String email, String logoutDevice, String logoutIp, LocalDateTime logoutTime) {
        this.userId = userId;
        this.email = email;
        this.logoutDevice = logoutDevice;
        this.logoutIp = logoutIp;
        this.logoutTime = logoutTime;
    }

    // All sessions constructor
    // Constructor
    public LogoutResponse(Long userId, int totalSessionsRevoked, List<SessionDto> revokedSessions, LocalDateTime timestamp) {
        this.userId = userId;
        this.totalSessionsRevoked = totalSessionsRevoked;
        this.revokedSessions = revokedSessions;
        this.timestamp = timestamp;
    }


// Getters
public Long getUserId() { return userId; }
public int getTotalSessionsRevoked() { return totalSessionsRevoked; }
public List<SessionDto> getRevokedSessions() { return revokedSessions; }
public LocalDateTime getTimestamp() { return timestamp; }

// Setters (optional if using Jackson)
public void setUserId(Long userId) { this.userId = userId; }
public void setTotalSessionsRevoked(int totalSessionsRevoked) { this.totalSessionsRevoked = totalSessionsRevoked; }
public void setRevokedSessions(List<SessionDto> revokedSessions) { this.revokedSessions = revokedSessions; }
public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}