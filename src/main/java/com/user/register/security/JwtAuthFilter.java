package com.user.register.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.register.repository.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserSessionRepository userSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ Skip filter for login & register APIs
        String path = request.getRequestURI();
        if (path.contains("/login") || path.contains("/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();

            try {
                // 1️⃣ Validate token
                String email = jwtUtil.validateAccessTokenAndGetUserId(token);

                // 2️⃣ Check if token exists in DB (active session)
                boolean sessionExists = userSessionRepository.findByToken(token).isPresent();
                if (!sessionExists) {
                    unauthorizedResponse(response, "User is logged out or token invalid");
                    return;
                }

                // 3️⃣ Check token expiry
                if (jwtUtil.extractExpiration(token).before(new java.util.Date())) {
                    userSessionRepository.findByToken(token)
                            .ifPresent(userSessionRepository::delete);

                    unauthorizedResponse(response, "Token expired. Please login again.");
                    return;
                }

                // 4️⃣ Set authentication
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, List.of());

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // ✅ Do not expose internal exception message
                unauthorizedResponse(response, "Invalid or expired token");
                return;
            }
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    // Helper method to send 401 JSON response
    private void unauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", message);
        resp.put("timestamp", LocalDateTime.now().toString());

        new ObjectMapper().writeValue(response.getWriter(), resp);
        SecurityContextHolder.clearContext();
    }
}