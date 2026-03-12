
package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // ✅ Generic constructor for any type T
    public ApiResponse(boolean b, String missingOrInvalidAuthorizationHeader, Object o) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now(); // auto set timestamp
    }
}