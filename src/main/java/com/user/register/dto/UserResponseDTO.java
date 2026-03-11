package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class UserResponseDTO {
    private Long id;
    private String email;
    private String password; // bcrypt
    private String firstName; // AES encrypted
    private String lastName;  // AES encrypted
    private String mobile;    // AES encrypted
    private String dob;       // AES encrypted
    private String city;      // AES encrypted
    private String state;     // AES encrypted
    private String country;   // AES encrypted
    private String organization; // AES encrypted
    private String profilePhoto; // String
    private String preferredLanguage; // plain
    private String skills;            // plain
    private String fieldOfStudy;      // plain
    private String highestQualification; // plain
    private String profilePhotoPath;  // file path
    private String status;
    private LocalDateTime createdAt;
    private String ipAddress;
    private String device;  // device type
    private String os;      // OS name
    private String browser; // browser name
    private String userAgent; // full User-Agent string
}