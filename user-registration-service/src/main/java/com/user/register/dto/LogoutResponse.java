package com.user.register.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class LogoutResponse {

    private UUID userId;
    private String email;
    private String logoutDevice;
    private String logoutIp;
    private LocalDateTime logoutTime;

    public LogoutResponse(
            UUID userId,
            String email,
            String logoutDevice,
            String logoutIp,
            LocalDateTime logoutTime
    ) {
        this.userId = userId;
        this.email = email;
        this.logoutDevice = logoutDevice;
        this.logoutIp = logoutIp;
        this.logoutTime = logoutTime;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getLogoutDevice() {
        return logoutDevice;
    }

    public String getLogoutIp() {
        return logoutIp;
    }

    public LocalDateTime getLogoutTime() {
        return logoutTime;
    }
}