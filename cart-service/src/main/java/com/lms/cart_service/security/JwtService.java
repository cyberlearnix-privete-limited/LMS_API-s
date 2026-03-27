package com.lms.cart_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY = "your_ultra_secure_secret_key_for_lms_project_2026";

    public String extractUserId(String token) {
        return extractClaim(cleanToken(token), Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(cleanToken(token), claims -> claims.get("roles", String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            final Date expiration = extractClaim(cleanToken(token), Claims::getExpiration);
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Helper to remove "Bearer " if present
    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}