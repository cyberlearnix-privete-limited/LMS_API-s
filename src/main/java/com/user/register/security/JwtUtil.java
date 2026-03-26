package com.user.register.security;

import com.user.register.entity.User;
import com.user.register.service.TokenBlacklistService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final TokenBlacklistService blacklistService;

    private static final String SECRET =
            "aVeryLongSuperSecureSecretKeyForJwtTokenGenerationWith256BitStrength123456789SecureKey";

    private static final String ISSUER = "user-service";

    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000;
    private static final long REFRESH_TOKEN_EXPIRATION = 30L * 24 * 60 * 60 * 1000;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // ================= TOKEN GENERATION =================
    private String generateToken(String subject, long expiration, String type, String role) {

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuer(ISSUER)
                .claim("type", type)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(String userId, String role) {
        return generateToken(userId, ACCESS_TOKEN_EXPIRATION, "access", role);
    }

    public String generateRefreshToken(User user, String userId, String role) {
        return generateToken(userId, REFRESH_TOKEN_EXPIRATION, "refresh", role);
    }

    // ================= CLAIM EXTRACTION =================
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .requireIssuer(ISSUER)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired");

        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ✅ FIXED: NO ROLE MODIFICATION HERE
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    // ================= VALIDATION =================
    public String validateAccessTokenAndGetUserId(String token) {

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token missing");
        }

        if (blacklistService.isBlacklisted(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token blacklisted");
        }

        Claims claims = extractAllClaims(token);

        if (!"access".equals(claims.get("type", String.class))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token type");
        }

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}