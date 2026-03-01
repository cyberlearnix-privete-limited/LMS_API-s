package com.user.register.controller;

import com.user.register.dto.*;
import com.user.register.entity.OTPCode;
import com.user.register.entity.User;
import com.user.register.entity.UserSession;
import com.user.register.exception.InvalidOtpException;
import com.user.register.exception.LoginFailedException;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import com.user.register.service.RegistrationService;
import com.user.register.service.SessionService;
import com.user.register.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;
    private final UserRepository userRepository;           // add this
    private final UserSessionRepository sessionRepository;

    private RegistrationService authService;
    private JwtUtil jwtUtil;
    private TokenBlacklistService blacklistService;
    private Object userId;
    private String token;
    private Object SessionService;

    @PostMapping(value = "/upload/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("file") MultipartFile file) {
        try {

            // 1️⃣ Check empty
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "File is empty", null, LocalDateTime.now()));
            }

            // 2️⃣ Validate size (5MB)
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "File size must be less than 5MB", null, LocalDateTime.now()));
            }

            // 3️⃣ Validate type
            String contentType = file.getContentType();
            if (contentType == null ||
                    !(contentType.equals("image/jpeg") ||
                            contentType.equals("image/png") ||
                            contentType.equals("image/webp"))) {

                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false,
                                "Only JPG, PNG, WEBP formats allowed",
                                null,
                                LocalDateTime.now()));
            }

            // 4️⃣ Read image
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid image file", null, LocalDateTime.now()));
            }

            // 5️⃣ Resize to 512x512
            BufferedImage resizedImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, 512, 512, null);
            g.dispose();

            // 6️⃣ Create uploads folder
            String uploadDir = "uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 7️⃣ Generate filename
            String extension = contentType.equals("image/png") ? "png"
                    : contentType.equals("image/webp") ? "webp"
                    : "jpg";

            String fileName = UUID.randomUUID() + "." + extension;
            File outputFile = new File(uploadDir + fileName);

            // 8️⃣ Save resized image
            ImageIO.write(resizedImage, extension.equals("jpg") ? "jpeg" : extension, outputFile);

            // 9️⃣ Generate URL
            String fileUrl = "http://localhost:8080/uploads/" + fileName;

            return ResponseEntity.ok(
                    new ApiResponse<>(true,
                            "Profile photo uploaded successfully (512x512)",
                            fileUrl,
                            LocalDateTime.now())
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    /**
     * Register user and send OTP
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            // Force role to STUDENT
            user.setRole(User.Role.STUDENT);
            user.setIsInstructorApproved(false);

            // ✅ Ensure profile photo is set if provided
            if (user.getProfilePhoto() != null && !user.getProfilePhoto().isBlank()) {
                user.setProfilePhoto(user.getProfilePhoto());
            }

            User savedUser = registrationService.register(user);
            ApiResponse<User> response = new ApiResponse<>(
                    true,
                    "User registered successfully. OTP has been sent to email.",
                    savedUser,
                    LocalDateTime.now()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Apply for instructor role
     */
    @PostMapping("/instructors/apply")
    public ResponseEntity<?> applyInstructor(@RequestBody User user) {
        try {
            // Force role to INSTRUCTOR and set approval false
            user.setRole(User.Role.INSTRUCTOR);
            user.setIsInstructorApproved(false);
            User savedUser = registrationService.register(user);
            ApiResponse<User> response = new ApiResponse<>(
                    true,
                    "Instructor application submitted. Please wait for admin approval.",
                    savedUser,
                    LocalDateTime.now()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String email, @RequestParam String otp) {
        try {
            Map<String, Object> response = registrationService.verifyOTP(email, otp);
            return ResponseEntity.ok(response); // OTP correct → 200
        } catch (InvalidOtpException ex) {
            // Wrong OTP → 400 with remaining attempts & expiry
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", ex.getMessage());
            errorResponse.put("data", Map.of(
                    "remainingAttempts", ex.getRemainingAttempts(),
                    "expiresInSeconds", ex.getSecondsUntilExpiry()
            ));
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException e) {
            // OTP expired, locked account, etc.
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    @PostMapping("/login/password")
    public ResponseEntity<?> loginWithPassword(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            // Call service to handle login
            LoginResponse loginResponse = registrationService.loginWithPassword(request);
            // 2️⃣ Fetch user from DB
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 3️⃣ Save session
            UserSession session = UserSession.builder()
                    .user(user)
                    .deviceInfo(String.valueOf(httpRequest.getHeader("User-Agent"))) // ✅ correct
                    .token(loginResponse.getAccessToken()) // store JWT
                    .expiresAt(LocalDateTime.now().plusDays(7)) // set expiry
                    .build();
            sessionRepository.save(session);

            ApiResponse<LoginResponse> response = new ApiResponse<>(
                    true,
                    "Login successful",
                    loginResponse,
                    LocalDateTime.now()
            );
           
            return ResponseEntity.ok(response);

        } catch (LoginFailedException ex) {
            // Catch failed login attempts to return detailed info
            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    ex.getMessage(),
                    ex.getDetails(), // remainingAttempts & accountStatus
                    LocalDateTime.now()
            );
            return ResponseEntity.status(401).body(response); // 401 Unauthorized
        } catch (RuntimeException e) {
            // Other exceptions
            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.status(400).body(response); // 400 Bad Request
        }
    }

    @PostMapping("/login/otp/request")
    public ResponseEntity<?> requestLoginOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            // 🔹 Get detailed response from service
            ApiResponse<Map<String, Object>> response =
                    registrationService.sendLoginOtp(email);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(
                            false,
                            e.getMessage(),
                            null,
                            LocalDateTime.now()
                    ));
        }
    }

    @PostMapping("/login/otp/verify")
    public ResponseEntity<?> verifyLoginOtp(@RequestBody Map<String, String> request) {

        try {

            String email = request.get("email");
            String otp = request.get("otp");

            LoginResponse loginResponse =
                    registrationService.verifyLoginOtp(email, otp);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "OTP verified successfully. Login completed.",
                            loginResponse,
                            LocalDateTime.now()
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(
                            false,
                            ex.getMessage(),
                            null,
                            LocalDateTime.now()
                    )
            );
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {

        try {
            String refreshToken = request.get("refreshToken");

            LoginResponse response = registrationService.refreshAccessToken(refreshToken);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Access token refreshed successfully.",
                            response,
                            LocalDateTime.now()
                    )
            );

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(
                            false,
                            ex.getMessage(),
                            null,
                            LocalDateTime.now()
                    ));
        }
    }

    // ================= FORGOT PASSWORD =================
    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            ForgotPasswordResponseData data = registrationService.sendResetOtp(request.getEmail());
            return ResponseEntity.ok(new ApiResponse<>(true, "OTP sent successfully", data, LocalDateTime.now()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    // ================= RESET PASSWORD =================
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            Map<String, Object> data = registrationService.resetPasswordWithOtp(
                    request.getEmail(),
                    request.getOtp(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "Password reset successfully", data, LocalDateTime.now()));
        } catch (InvalidOtpException ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", ex.getMessage());
            errorResponse.put("data", Map.of(
                    "remainingAttempts", ex.getRemainingAttempts(),
                    "expiresInSeconds", ex.getSecondsUntilExpiry()
            ));
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }
    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ApiResponse<>(
                    false,
                    "Authorization header missing or invalid",
                    null,
                    LocalDateTime.now()
            );
        }
        String accessToken = authHeader.substring(7); // remove "Bearer "

        // ===================== Friendly device detection =====================
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = "Other Device"; // default

        if (userAgent != null && !userAgent.isBlank()) {
            String uaLower = userAgent.toLowerCase();
            if (uaLower.contains("mozilla") || uaLower.contains("chrome") || uaLower.contains("firefox")
                    || uaLower.contains("safari") || uaLower.contains("edge")) {
                deviceInfo = "Web Browser";
            } else if (uaLower.contains("android") || uaLower.contains("iphone") || uaLower.contains("ipad")
                    || uaLower.contains("mobile")) {
                deviceInfo = "Mobile App";
            } else if (uaLower.contains("postman")) {
                deviceInfo = "API Client";
            }
        }

        // ===================== Logout in service =====================
        Map<String, Object> data = registrationService.logoutCurrentDevice(
                accessToken,
                request.getRemoteAddr(),
                deviceInfo
        );

        return new ApiResponse<>(
                true,
                "Logout successful",
                data,
                LocalDateTime.now()
        );
    }
}