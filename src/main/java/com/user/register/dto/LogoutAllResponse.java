package com.user.register.dto;

import com.user.register.dto.SessionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class LogoutAllResponse {

    private Long userId;
    private int totalSessionsRevoked;
    private List<SessionDto> revokedSessions;
    private LocalDateTime timestamp;

}