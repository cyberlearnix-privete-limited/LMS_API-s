package com.lms.courseservice.config;

import com.lms.courseservice.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // Auth API
                        .requestMatchers("/auth/**").permitAll()

                        // Public access
                        .requestMatchers(HttpMethod.GET, "/sections/*/lectures")
                        .hasAnyRole("STUDENT","INSTRUCTOR","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/courses/*/sections").permitAll()

                        // ✅ STUDENT enroll (put BEFORE /courses/**)
                        .requestMatchers(HttpMethod.POST, "/courses/*/enroll")
                        .hasRole("STUDENT")

                        // Instructor/Admin manage courses
                        .requestMatchers(HttpMethod.POST, "/courses/**").hasAnyRole("INSTRUCTOR","ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/courses/**").hasAnyRole("INSTRUCTOR","ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/courses/**").hasAnyRole("INSTRUCTOR","ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses/**").hasAnyRole("INSTRUCTOR","ADMIN")

                        // Sections modify
                        .requestMatchers(HttpMethod.POST, "/courses/*/sections").hasAnyRole("INSTRUCTOR","ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/courses/sections/*").hasAnyRole("INSTRUCTOR","ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses/sections/*").hasAnyRole("INSTRUCTOR","ADMIN")

                        // Lectures
                        .requestMatchers(HttpMethod.GET, "/sections/*/lectures").permitAll()

                        .requestMatchers(HttpMethod.POST, "/sections/*/lectures")
                        .hasAnyRole("INSTRUCTOR","ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/sections/*/lectures/*")
                        .hasAnyRole("INSTRUCTOR","ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/sections/*/lectures/*")
                        .hasAnyRole("INSTRUCTOR","ADMIN")

                        .requestMatchers(HttpMethod.POST, "/courses/*/preview/*")
                        .hasAnyRole("INSTRUCTOR","ADMIN")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}