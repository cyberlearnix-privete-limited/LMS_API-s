package com.user.register.service;

import com.user.register.dto.LogoutResponse;
import com.user.register.entity.UserSession;
import com.user.register.entity.User;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionService {

    private final UserSessionRepository sessionRepository;

    public SessionService(UserSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public List<UserSession> getSessionsForUser(User user) {
        return sessionRepository.findByUser(user);
    }

    public LogoutResponse logoutDevice(String token, Long sessionId, HttpServletRequest request) {
        // Remove Bearer prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // ✅ CORRECT CALL
        String email = JwtUtil.validateAgit ccessTokenAndGetEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserSession session = sessionRepository
                .findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        sessionRepository.delete(session);

        return new LogoutResponse(
                user.getId(),
                user.getEmail(),
                session.getDeviceInfo(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        );
    }
}