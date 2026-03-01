package com.user.register.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

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

    private Integer failedLoginAttempts = 0;

    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;

    private Boolean isInstructorApproved = false;
    private LocalDateTime lastLogin;  // <-- add this

    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
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
    public enum Status { PENDING_VERIFICATION, ACTIVE, LOCKED, SUSPENDED, DELETED }
    public enum Role { STUDENT, INSTRUCTOR, ADMIN}
}




