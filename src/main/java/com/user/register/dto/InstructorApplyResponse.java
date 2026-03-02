package com.user.register.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.user.register.dto.SessionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL) // ⬅ Omit null fields
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorApplyResponse {

    private Long userId;                  // User ID
    private String firstName;             // Decrypted first name
    private String lastName;              // Decrypted last name
    private String email;                 // User email
    private String mobile;                // Decrypted mobile
    private String dob;                   // Decrypted DOB
    private String profilePhoto;          // Profile photo URL
    private String city;                  // Decrypted city
    private String state;                 // Decrypted state
    private String country;               // Decrypted country
    private String preferredLanguage;     // Preferred language
    private String organization;          // Decrypted organization
    private String skills;                // Skills
    private String fieldOfStudy;          // Field of study
    private String highestQualification;  // Highest qualification
    private String role;                  // Current role (STUDENT/INSTRUCTOR)
    private String appliedRole;           // Applied role (INSTRUCTOR)
    private String applicationStatus;     // PENDING_VERIFICATION / APPROVED / REJECTED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
}