package com.user.register.security;

import com.user.register.entity.User;
import com.user.register.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // get provider name
        OAuth2AuthenticationToken tokenAuth = (OAuth2AuthenticationToken) authentication;
        String provider = tokenAuth.getAuthorizedClientRegistrationId(); // google/github

        // get email
        String email = oAuth2User.getAttribute("email");

        // fallback for GitHub
        if (email == null) {
            email = oAuth2User.getAttribute("login") + "@github.com";
        }

        // save or login user
        User user = userService.socialLogin(email, provider);

        // generate JWT
        String token = jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getRole().name()
        );

        // response JSON
        response.setContentType("application/json");
        response.getWriter().write(
                "{ \"email\": \"" + email + "\", \"token\": \"" + token + "\", \"provider\": \"" + provider + "\" }"
        );
    }
}