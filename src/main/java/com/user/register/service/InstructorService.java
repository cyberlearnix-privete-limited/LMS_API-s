package com.user.register.service;

import com.user.register.dto.InstructorApplyResponse;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InstructorService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;

    public InstructorService(UserRepository userRepository,
                             UserSessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public InstructorApplyResponse applyForInstructor(HttpServletRequest request) {
        // 1️⃣ Get userId from request attribute (set by JwtAuthFilter)
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        UUID userId = (UUID) userIdAttr;

        // 2️⃣ Load user and check if account is active
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Account temporarily locked until " + user.getLockedUntil());
        }

        // 3️⃣ Check if user already applied
        if (user.getAppliedRole() == User.Role.INSTRUCTOR
                && user.getApplicationStatus() == User.ApplicationStatus.PENDING_VERIFICATION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instructor application already pending");
        }

        // 4️⃣ Update applied role and application status
        user.setAppliedRole(User.Role.INSTRUCTOR);
        user.setApplicationStatus(User.ApplicationStatus.PENDING_VERIFICATION);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 5️⃣ Build detailed response
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
                user.getRole() != null ? user.getRole().name() : null,
                user.getAppliedRole() != null ? user.getAppliedRole().name() : null,
                user.getApplicationStatus() != null ? user.getApplicationStatus().name() : null,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin()
        );
    }

    private String decrypt(String value) {
        if (value == null) return null;
        try {
            return SecurityUtils.decrypt(value, "1234567890123456");
        } catch (Exception e) {
            return value;
        }
    }
}