package com.user.register.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class  User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String confirmPassword;
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
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING_VERIFICATION;
    @Enumerated(EnumType.STRING)
    private Role appliedRole; // INSTRUCTOR (temporary)
    private Integer failedLoginAttempts = 0;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;
    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;
    private Boolean isInstructorApproved = false;
    private LocalDateTime lastLogin;  // <-- add this
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private String ipAddress;
    private String device;
    private String browser;
    private String os;

    private String userAgent;
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    // ✅ Timestamp for rate-limiting
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public void setRefreshToken(String refreshToken) {
    }

    public enum Status { PENDING_VERIFICATION, ACTIVE, LOCKED, SUSPENDED, DELETED }
    public enum Role { STUDENT, INSTRUCTOR, ADMIN}
    public enum ApplicationStatus {
        PENDING_VERIFICATION,
        APPROVED,
        REJECTED
    }
}




