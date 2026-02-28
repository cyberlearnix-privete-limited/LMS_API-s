package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.LogoutResponse;
import com.user.register.dto.UserSessionsResponse;
import com.user.register.entity.User;
import com.user.register.entity.UserSession;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class SessionController {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public SessionController(UserSessionRepository sessionRepository,
                             UserRepository userRepository,
                             JwtUtil jwtUtil) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/sessions")
    public ApiResponse<UserSessionsResponse> listSessions(HttpServletRequest request) {
        try {
            // 1️⃣ Extract JWT from header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new ApiResponse<>(false, "Missing or invalid Authorization header", null);
            }
            String token = authHeader.substring(7);

            // 2️⃣ Get user ID from JWT
            Long userId;
            try {
                userId = Long.parseLong(jwtUtil.validateAccessTokenAndGetEmail(token)); // returns user ID
            } catch (Exception ex) {
                return new ApiResponse<>(false, "Invalid or expired token", null);
            }

            // 3️⃣ Fetch user by ID
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return new ApiResponse<>(false, "No user found for user ID: " + userId, null);
            }

            // 4️⃣ Fetch all active sessions
            List<UserSession> sessions = sessionRepository.findByUser(user)
                    .stream()
                    .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());

            // 5️⃣ Prepare detailed response
            UserSessionsResponse response = new UserSessionsResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    sessions
            );

            return new ApiResponse<>(true, "Sessions fetched successfully", response);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Internal server error: " + e.getMessage(), null);
        }
    }
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Object>> logoutDevice(
            @PathVariable("id") Long sessionId,
            @RequestHeader("Authorization") String token,
            HttpServletRequest httpRequest) {

        try {
            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 1️⃣ Get user from JWT
            String email = jwtUtil.validateAccessTokenAndGetEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

            // 2️⃣ Find the session for this user
            UserSession session = sessionRepository.findByIdAndUser(sessionId, user)
                    .orElseThrow(() -> new RuntimeException("Session not found or does not belong to user"));

            // 3️⃣ Delete the session
            sessionRepository.delete(session);

            // 4️⃣ Build detailed response
            ApiResponse<Object> response = new ApiResponse<>(
                    true,
                    "Device logged out successfully",
                    new LogoutResponse(user.getId(), user.getEmail(), session.getDeviceInfo(),
                            httpRequest.getRemoteAddr(), LocalDateTime.now()),
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.status(400).body(errorResponse);
        }
    }
}