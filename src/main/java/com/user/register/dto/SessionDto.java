package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@JsonInclude(JsonInclude.Include.NON_NULL) // ✅ ignore null fields

@Data
@AllArgsConstructor   // generates constructor with all fields
@NoArgsConstructor    // generates default no-arg constructor
public class SessionDto {
    private Long id;
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime loginTime;
    private String email;
}