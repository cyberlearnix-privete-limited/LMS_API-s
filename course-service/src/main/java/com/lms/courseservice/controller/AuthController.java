package com.lms.courseservice.controller;

import com.lms.courseservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String username,
                                     @RequestParam String role) {

        // ✅ Convert username → UUID
        UUID userId = UUID.fromString(username);

        String token = jwtUtil.generateToken(userId, role);

        return Map.of(
                "access_token", token
        );
    }
}