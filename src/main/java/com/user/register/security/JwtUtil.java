package com.user.register.security;

import com.user.register.entity.User;
import com.user.register.service.TokenBlacklistService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor  // ✅ generates constructor for final fields

public class JwtUtil {

    private static final String SECRET =
            "my-super-secret-key-my-super-secret-key-1234567890";

    private static final String ISSUER = "user-service";
    private final TokenBlacklistService blacklistService; // ✅ inject blacklist service

    // ✅ Expiry constants
    private static final long ACCESS_TOKEN_EXPIRATION = 15L * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 30L * 24 * 60 * 60 * 1000; // 30 days

    private final SecretKey secretKey =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    // 🔐 Base token generator
    public String generateToken(String userId, long expirationMs, String type) {

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .setIssuer(ISSUER)
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Access Token (15 minutes)
    public String generateAccessToken(String email) {
        return generateToken(String.valueOf(email), ACCESS_TOKEN_EXPIRATION, "access");
    }

    // ✅ Refresh Token (30 days)
    public String generateRefreshToken(User user, String email) {
        return generateToken(email, REFRESH_TOKEN_EXPIRATION, "refresh");
    }

    // =========================================================
    // 🔎 COMMON CLAIM EXTRACTOR
    // =========================================================
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .requireIssuer(ISSUER)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // =========================================================
    // 🔎 EXTRACT METHODS
    // =========================================================
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // =========================================================
    // 🔎 VALIDATION METHODS
    // =========================================================

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // 🔍 Validate refresh token only
    public String validateRefreshTokenAndGetUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);

            if (!"refresh".equals(claims.get("type", String.class))) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid token type"
                );
            }

            return claims.getSubject();

        } catch (JwtException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired refresh token"
            );
        }
    }
    // Remove static keyword

    // =========================================================
    // ⏳ EXPIRY HELPERS (For API response)
    // =========================================================

    public long getAccessTokenExpirySeconds() {
        return ACCESS_TOKEN_EXPIRATION / 1000;
    }

    public long getRefreshTokenExpirySeconds() {
        return REFRESH_TOKEN_EXPIRATION / 1000;
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    public String validateAccessTokenAndGetUserId(String token) {
        if (blacklistService.isBlacklisted(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is logged out. Please login again.");
        }
        Claims claims = extractAllClaims(token);
        if (!"access".equals(claims.get("type", String.class))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token type");
        }
        return claims.getSubject();
    }
    // Optional: generate JWT for testing
    public String generateToken(String email, long expirationMs) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS256, SECRET.getBytes())
                .compact();
    }
}