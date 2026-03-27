package com.user.register.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponseData {
    private String email;
    private LocalDateTime passwordChangedAt;
    private String message;
}
