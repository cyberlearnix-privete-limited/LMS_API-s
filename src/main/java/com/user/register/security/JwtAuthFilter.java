package com.user.register.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Allow anonymous access to public auth operations only
        return path.equals("/auth/register")
                || path.equals("/auth/verify-email")
                || path.equals("/auth/login/password")
                || path.equals("/auth/login/otp/request")
                || path.equals("/auth/login/otp/verify")
                || path.equals("/auth/password/forgot")
                || path.equals("/auth/password/reset")
                || path.equals("/auth/refresh")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // ✅ validate token
            String userId = jwtUtil.validateAccessTokenAndGetUserId(token);

            // ✅ extract role
            String role = jwtUtil.extractRole(token);

            if (role == null || role.isBlank()) {
                role = "USER";
            }

            // 🔥 FIX: remove ROLE_ duplication + normalize
            role = role.replace("ROLE_", "").toUpperCase();

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + role);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(authority)
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

            request.setAttribute("userId", userId);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write(
                    "{ \"error\": \"Unauthorized\", \"message\": \"" + e.getClass().getSimpleName() + ": " + e.getMessage() + "\" }"
            );
        }
    }

    private String extractToken(HttpServletRequest request) {
        // 1) Prefer explicit bearer header
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }

        // 2) Fallback to access token cookie (login writes this cookie)
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "accessToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}