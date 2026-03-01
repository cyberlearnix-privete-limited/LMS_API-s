package com.user.register.service;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.SessionDto;
import com.user.register.dto.UserProfileResponse;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import com.user.register.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstructorService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final JwtUtil jwtUtil;

    public InstructorService(UserRepository userRepository,
                             UserSessionRepository sessionRepository,
                             JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.jwtUtil = jwtUtil;
    }

    public UserProfileResponse applyForInstructor(HttpServletRequest request) {
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

        // 3️⃣ Update role and status
        user.setRole(User.Role.INSTRUCTOR);
        user.setStatus(User.Status.PENDING_VERIFICATION);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 4️⃣ Get active sessions
        List<SessionDto> activeSessions = sessionRepository.findByUser(user)
                .stream()
                .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(s -> new SessionDto(
                        s.getId(),
                        s.getDeviceInfo(),
                        s.getIpAddress(),
                        s.getCreatedAt(),
                        user.getEmail()
                ))
                .collect(Collectors.toList());

        // 5️⃣ Build detailed response
        return new UserProfileResponse(
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
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin(),
                null
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
