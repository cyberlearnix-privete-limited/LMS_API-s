package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.LogoutAllResponse;
import com.user.register.dto.LogoutResponse;
import com.user.register.dto.SessionDto;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import com.user.register.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class SessionController {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final SessionService sessionService;
    private byte[] secretKey;

    public SessionController(UserSessionRepository sessionRepository,
                             UserRepository userRepository,
                             JwtUtil jwtUtil,
                             SessionService sessionService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.sessionService = sessionService;
    }

    @GetMapping("/sessions")
    public ApiResponse<List<SessionDto>> listSessions(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ApiResponse<>(false, "Missing or invalid Authorization header", null);
        }

        String token = authHeader.substring(7);
        Long userId = Long.parseLong(jwtUtil.validateAccessTokenAndGetUserId(token));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SessionDto> sessions = sessionRepository.findByUser(user)
                .stream()
                .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(s -> new SessionDto(
                        s.getId(),
                        user.getId(),
                        s.getDeviceInfo(),
                        s.getIpAddress(),   // IP now saved
                        s.getCreatedAt(),
                        user.getEmail()     // email added
                ))
                .toList();

        return new ApiResponse<>(
                true,
                "Sessions fetched successfully",
                sessions,
                LocalDateTime.now()
        );
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Object>> logoutDevice(
            @PathVariable("id") Long sessionId,
            HttpServletRequest request) {

        try {

            LogoutResponse response = sessionService.logoutDevice(sessionId, request);

            return ResponseEntity.status(200).body(
                    new ApiResponse<>(
                            true,
                            "Device logged out successfully",
                            response,
                            LocalDateTime.now()
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.status(404).body(
                    new ApiResponse<>(
                            false,
                            e.getMessage(),
                            null,
                            LocalDateTime.now()
                    )
            );
        }
    }

    @DeleteMapping("/sessions/all")
    public ResponseEntity<ApiResponse<Object>> logoutAllSessions(HttpServletRequest request) {

        try {

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(
                        new ApiResponse<>(
                                false,
                                "Missing or invalid Authorization header",
                                null,
                                LocalDateTime.now()
                        )
                );
            }

            String token = authHeader.substring(7);

            Long userId = Long.parseLong(jwtUtil.validateAccessTokenAndGetUserId(token));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<SessionDto> revokedSessions = sessionService.logoutAllSessions(user);

            LogoutAllResponse response = new LogoutAllResponse(
                    user.getId(),
                    revokedSessions.size(),
                    revokedSessions,
                    LocalDateTime.now()
            );

            return ResponseEntity.status(200).body(
                    new ApiResponse<>(
                            true,
                            "Logged out from all sessions successfully",
                            response,
                            LocalDateTime.now()
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.status(400).body(
                    new ApiResponse<>(
                            false,
                            e.getMessage(),
                            null,
                            LocalDateTime.now()
                    )
            );
        }
    }
}