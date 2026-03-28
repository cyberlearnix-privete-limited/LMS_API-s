package com.lms.coupon_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for Postman testing
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // This allows ALL requests without a token
                );
        // .oauth2ResourceServer(...)  <-- Commented out to stop checking for JWT

        return http.build();
    }

    // You can keep or comment out the jwtDecoder() bean;
    // since oauth2ResourceServer is off, Spring won't look for it.
}