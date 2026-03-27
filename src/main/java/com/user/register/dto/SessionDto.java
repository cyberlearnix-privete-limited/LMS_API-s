package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionDto {

    private UUID id;
    private String deviceInfo;
    private UUID userId;

    private String ipAddress;
    private LocalDateTime loginTime;
    private String email;

    public SessionDto(
            UUID id,
            UUID userId,
            String deviceInfo,
            String ipAddress,
            LocalDateTime loginTime,
            String email
    ) {
        this.id = id;
        this.userId = userId;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.loginTime = loginTime;
        this.email = email;
    }
}