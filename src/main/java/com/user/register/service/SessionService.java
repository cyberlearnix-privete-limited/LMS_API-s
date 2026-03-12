package com.user.register.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.user.register.dto.LogoutResponse;
import com.user.register.dto.SessionDto;
import com.user.register.entity.UserSession;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL) // ignore nulls

@Service
public class SessionService {

    // ✅ Declare ALL required dependencies
    private final JwtUtil jwtUtil;
    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    // ✅ Constructor injection for ALL dependencies
    public SessionService(JwtUtil jwtUtil,
                          UserSessionRepository sessionRepository,
                          UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    // Create session at login
    public void createSession(User user, HttpServletRequest request) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setDeviceInfo(request.getHeader("User-Agent"));
        session.setIpAddress(getClientIp(request));
        session.setCreatedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    public List<UserSession> getSessionsForUser(User user) {
        return sessionRepository.findByUser(user);
    }

    // Logout single device
    public LogoutResponse logoutDevice(Long sessionId, HttpServletRequest request) {

        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        User user = session.getUser();

        LogoutResponse response = new LogoutResponse(
                user.getId(),
                user.getEmail(),
                session.getDeviceInfo(),
                getClientIp(request),   // ✅ correct IP extraction
                LocalDateTime.now()
        );

        sessionRepository.delete(session);

        return response;
    }

    public List<SessionDto> logoutAllSessions(User user) {

        List<UserSession> activeSessions = sessionRepository.findByUser(user)
                .stream()
                .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))
                .toList();

        List<SessionDto> revokedSessions = activeSessions.stream()
                .map(s -> new SessionDto(
                        s.getId(),
                        user.getId(),
                        s.getDeviceInfo(),
                        s.getIpAddress(),
                        s.getCreatedAt(),
                        user.getEmail()
                ))
                .toList();

        sessionRepository.deleteAll(activeSessions);

        return revokedSessions;
    }

    private String getClientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}