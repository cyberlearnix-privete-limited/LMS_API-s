package com.user.register.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LogoutAllResponse {

    private UUID userId;
    private int totalSessionsRevoked;
    private List<SessionDto> revokedSessions;
    private LocalDateTime timestamp;
}