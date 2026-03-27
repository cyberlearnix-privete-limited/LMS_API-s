package com.user.register.dto;



import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    private String status; // ACTIVE / INACTIVE
    private String applicationStatus;
}