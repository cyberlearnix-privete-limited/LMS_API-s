package com.user.register.dto;

import java.time.LocalDateTime;

public  class LogoutResponse {
    private Long userId;
    private String email;
    private String logoutDevice;
    private String logoutIp;
    private LocalDateTime logoutTime;

    public LogoutResponse(Long userId, String email, String logoutDevice, String logoutIp, LocalDateTime logoutTime) {
        this.userId = userId;
        this.email = email;
        this.logoutDevice = logoutDevice;
        this.logoutIp = logoutIp;
        this.logoutTime = logoutTime;
    }

    // Getters (or use Lombok @Data)
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getLogoutDevice() { return logoutDevice; }
    public String getLogoutIp() { return logoutIp; }
    public LocalDateTime getLogoutTime() { return logoutTime; }
}

