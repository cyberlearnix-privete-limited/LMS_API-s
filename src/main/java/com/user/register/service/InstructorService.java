package com.user.register.service;

import com.user.register.dto.InstructorApplyResponse;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import com.user.register.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InstructorService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final JwtUtil jwtUtil;
    private byte[] secretKey;

    public InstructorService(UserRepository userRepository,
                             UserSessionRepository sessionRepository,
                             JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.jwtUtil = jwtUtil;
    }
    public InstructorApplyResponse applyForInstructor(HttpServletRequest request) {
        // 1️⃣ Extract JWT
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        Long userId = Long.parseLong(jwtUtil.validateAccessTokenAndGetUserId(token));
        // 2️⃣ Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3️⃣ Update applied role and application status
        user.setAppliedRole(User.Role.INSTRUCTOR);
        user.setApplicationStatus(User.ApplicationStatus.PENDING_VERIFICATION);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 4️⃣ Build detailed response (no active sessions)
        return new InstructorApplyResponse(
                user.getId(),
                decrypt(user.getFirstName()),
                decrypt(user.getLastName()),
                user.getEmail(),
                decrypt(user.getMobile()),
                decrypt(user.getDob()),
                user.getProfilePhoto(),
                decrypt(user.getCity()),
                decrypt(user.getState()),
                decrypt(user.getCountry()),
                user.getPreferredLanguage(),
                decrypt(user.getOrganization()),
                user.getSkills(),
                user.getFieldOfStudy(),
                user.getHighestQualification(),
                user.getRole() != null ? user.getRole().name() : null,                // role
                user.getAppliedRole() != null ? user.getAppliedRole().name() : null,  // appliedRole
                user.getApplicationStatus() != null ? user.getApplicationStatus().name() : null, // applicationStatus
                user.getCreatedAt(),   // LocalDateTime
                user.getUpdatedAt(),   // LocalDateTime
                user.getLastLogin()   // LocalDateTime
        );
    }
    private String decrypt(String value) {
        if (value == null) return null;
        try {
            return SecurityUtils.decrypt(value, "1234567890123456"); // use your encryption key
        } catch (Exception e) {
            return value;
        }
    }
}
