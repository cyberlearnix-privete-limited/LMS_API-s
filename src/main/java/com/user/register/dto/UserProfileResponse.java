package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
@JsonInclude(JsonInclude.Include.NON_NULL) // ⬅ Omit null fields
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String dob;
    private String profilePhoto;
    private String city;
    private String state;
    private String country;
    private String preferredLanguage;
    private String organization;
    private String skills;
    private String fieldOfStudy;
    private String highestQualification;
    // UserProfileResponse.java
    private String role; // single role as string
    private String status; // ACTIVE/SUSPENDED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private List<SessionDto> activeSessions;
}