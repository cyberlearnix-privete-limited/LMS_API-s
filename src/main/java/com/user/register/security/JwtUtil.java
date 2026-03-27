package com.user.register.security;

import com.user.register.entity.User;
import com.user.register.service.TokenBlacklistService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final TokenBlacklistService blacklistService;

    public JwtUtil(TokenBlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    private static final String SECRET =
            "aVeryLongSuperSecureSecretKeyForJwtTokenGenerationWith256BitStrength123456789SecureKey";

    private static final String ISSUER = "user-service";

    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000;
    private static final long REFRESH_TOKEN_EXPIRATION = 30L * 24 * 60 * 60 * 1000;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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

    // ================= CLAIMS =================
    private Claims getClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token missing");
        }

        if (blacklistService.isBlacklisted(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token blacklisted");
        }

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
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }


    public String validateAccessTokenAndGetUserId(String token) {
        Claims claims = getClaims(token);

        String type = claims.get("type", String.class);

        if (!"access".equalsIgnoreCase(type)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not an access token");
        }

        return claims.getSubject();
    }
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}