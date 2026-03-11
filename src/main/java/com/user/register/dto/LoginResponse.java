package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "userId", "email", "firstName", "lastName", "mobile", "dob",
        "profilePhoto", "city", "state", "country", "organization", "preferredLanguage",
        "skills", "fieldOfStudy", "highestQualification", "role", "status",
        "isInstructorApproved", "failedLoginAttempts",
        "createdAt", "updatedAt", "lastLoginAt",
        "loginDevice", "loginIp", "sessionId","Os","browser","useragent",
        "accessToken", "refreshToken", "expiresInSeconds"
})
public class LoginResponse {

    // ✅ Tokens
    private String accessToken;
    private String refreshToken;
    private long expiresInSeconds;

    // ✅ User info
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String mobile;
    private String dob;
    private String profilePhoto;
    private String city;
    private String state;
    private String country;
    private String organization;
    private String preferredLanguage;
    private String skills;
    private String fieldOfStudy;
    private String highestQualification;
    private String role;
    private String status;
    private Boolean isInstructorApproved;
    private Integer failedLoginAttempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ Session info
    private LocalDateTime lastLoginAt;
    private String loginDevice;
    private String loginIp;
    private String sessionId;
    private String device;
    private String browser;
    private String os;
    private String userAgent;



    public void setAccessTokenExpiresAt(LocalDateTime localDateTime) {
    }

    public void setRefreshTokenExpiresAt(LocalDateTime localDateTime) {
    }

    public void setTokenType(String bearer) {
    }

    public void setAccessTokenExpiresInMinutes(long l) {
    }

    public void setRefreshTokenExpiresInDays(long l) {
    }

    public void setLoginTime(LocalDateTime now) {
    }
}