package com.user.register.config;

import com.user.register.security.JwtAuthFilter;
import com.user.register.security.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // ✅ PUBLIC
                        .requestMatchers(
                                "/auth/**",
                                "/auth/register",
                                "/auth/verify-email",
                                "/auth/login/password",
                                "/auth/login/otp/request",
                                "/auth/login/otp/verify",
                                "/auth/password/forgot",
                                "/auth/password/reset",
                                "/auth/refresh",
                                "/oauth2/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ✅ AUTH-ONLY for token-based actions
                        .requestMatchers("/auth/logout", "/auth/switch-role")
                        .authenticated()

                        // ✅ FIX: instructor apply endpoint MUST be defined
                        .requestMatchers("/instructor/apply")
                        .hasAnyRole("STUDENT", "INSTRUCTOR")
                        // ✅ INSTRUCTOR APIs
                        .requestMatchers("/instructor/**")
                        .hasRole("INSTRUCTOR")

                        // ✅ USER APIs
                        .requestMatchers("/users/**")
                        .hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")

                        // ✅ AUTH REQUIRED
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint((req, res, ex) -> {
                            res.setContentType("application/json");
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                )

                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler)
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}