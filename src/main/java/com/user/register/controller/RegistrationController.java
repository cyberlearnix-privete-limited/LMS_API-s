package com.user.register.controller;

import com.user.register.dto.*;
import com.user.register.entity.User;
import com.user.register.entity.UserSession;
import com.user.register.exception.InvalidOtpException;
import com.user.register.exception.LoginFailedException;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import com.user.register.service.RegistrationService;
import com.user.register.service.TokenBlacklistService;
import com.user.register.util.SecurityUtils;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.security.auth.login.AccountLockedException;
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
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;
    private final String encryptionKey = "my-secret-key";

    private RegistrationService authService;
    private Object userId;
    private String token;
    private Object SessionService;
    private Object user;
    private String browser;

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
    public ResponseEntity<?> register(@RequestBody User user, HttpServletRequest request) {
        try {

            // Force role to STUDENT
            user.setRole(User.Role.STUDENT);
            user.setIsInstructorApproved(false);

            // Ensure profile photo is set if provided
            if (user.getProfilePhoto() != null && !user.getProfilePhoto().isBlank()) {
                user.setProfilePhoto(user.getProfilePhoto());
            }

            // Pass request to service
            User savedUser = registrationService.register(user, request);

            ApiResponse<User> response = new ApiResponse<>(
                    true,
                    "User registered successfully. OTP has been sent to email.",
                    savedUser,
                    LocalDateTime.now()
            );

            // 201 → resource created
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {

            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()
            );

            // 400 → validation error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (RuntimeException e) {

            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()
            );

            // 409 → conflict (email/mobile already exists)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {

            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    "Internal server error",
                    null,
                    LocalDateTime.now()
            );

            // 500 → server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @RequestBody Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        String email = body.get("email");
        String otp = body.get("otp");

        try {
            // Call service (pass response for cookies)
            ResponseEntity<Map<String, Object>> serviceResponse =
                    registrationService.verifyOTP(email, otp, request, response);
            return serviceResponse;
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

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", Map.of(
                    "remainingAttempts", 0,
                    "expiresInSeconds", 0
            ));
            errorResponse.put("timestamp", LocalDateTime.now());

            String msg = e.getMessage().toLowerCase();
            HttpStatus status;

            if (msg.contains("locked")) {
                status = HttpStatus.LOCKED; // 423
            } else if (msg.contains("suspended")) {
                status = HttpStatus.FORBIDDEN; // 403
            } else if (msg.contains("not found")) {
                status = HttpStatus.NOT_FOUND; // 404
            } else if (msg.contains("expired")) {
                status = HttpStatus.BAD_REQUEST; // 400
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
            }
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    @PostMapping("/login/password")
    public ResponseEntity<?> loginWithPassword(@RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {

        try {
            // 1️⃣ Authenticate user via service
            ResponseEntity<Map<String, Object>> loginResponse =
                    registrationService.loginWithPassword(request, httpRequest, httpResponse);

            // 2️⃣ Fetch user
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 3️⃣ Generate tokens using JwtUtil
            String accessToken = jwtUtil.generateAccessToken(user.getId().toString(), user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(user, user.getId().toString(), user.getRole().name());


            LocalDateTime now = LocalDateTime.now();
            LocalDateTime accessTokenExpiry = now.plusMinutes(15);
            LocalDateTime refreshTokenExpiry = now.plusDays(30);

            // 4️⃣ Get device + IP
            String deviceInfo = httpRequest.getHeader("User-Agent");
            if (deviceInfo == null) deviceInfo = "Unknown Device";

            String ipAddress = httpRequest.getRemoteAddr();

            // 5️⃣ Save session
            UserAgent userAgent = UserAgent.parseUserAgentString(deviceInfo);
            String browser = userAgent.getBrowser().getName();
            String os = userAgent.getOperatingSystem().getName();
            UserSession session = UserSession.builder()
                    .user(user)
                    .accessToken(accessToken)    // ✅ set accessToken
                    .refreshToken(refreshToken)
                    .deviceInfo(deviceInfo)
                    .ipAddress(ipAddress)
                    .expiresAt(refreshTokenExpiry)
                    .build();
            sessionRepository.save(session);

            // 6️⃣ Set HttpOnly cookies for tokens
            Cookie accessCookie = new Cookie("accessToken", accessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(false);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(15 * 60); // 15 mins
            httpResponse.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            httpResponse.addCookie(refreshCookie);

            // 7️⃣ Prepare encrypted response
            LoginResponse responseData = new LoginResponse();

            responseData.setAccessToken(accessToken);
            responseData.setRefreshToken(refreshToken);
            responseData.setExpiresInSeconds(900);

            responseData.setUserId(user.getId());

            responseData.setEmail(SecurityUtils.encrypt(user.getEmail(), encryptionKey));
            responseData.setFirstName(SecurityUtils.encrypt(user.getFirstName(), encryptionKey));
            responseData.setLastName(SecurityUtils.encrypt(user.getLastName(), encryptionKey));
            responseData.setMobile(SecurityUtils.encrypt(user.getMobile(), encryptionKey));

            responseData.setDob(SecurityUtils.encrypt(user.getDob(), encryptionKey));
            responseData.setProfilePhoto(SecurityUtils.encrypt(user.getProfilePhoto(), encryptionKey));
            responseData.setCity(SecurityUtils.encrypt(user.getCity(), encryptionKey));
            responseData.setState(SecurityUtils.encrypt(user.getState(), encryptionKey));
            responseData.setCountry(SecurityUtils.encrypt(user.getCountry(), encryptionKey));
            responseData.setOrganization(SecurityUtils.encrypt(user.getOrganization(), encryptionKey));

            responseData.setPreferredLanguage(SecurityUtils.encrypt(user.getPreferredLanguage(), encryptionKey));
            responseData.setSkills(SecurityUtils.encrypt(user.getSkills(), encryptionKey));
            responseData.setFieldOfStudy(SecurityUtils.encrypt(user.getFieldOfStudy(), encryptionKey));
            responseData.setHighestQualification(SecurityUtils.encrypt(user.getHighestQualification(), encryptionKey));

            responseData.setRole(SecurityUtils.encrypt(user.getRole().name(), encryptionKey));
            responseData.setStatus(SecurityUtils.encrypt(user.getStatus().name(), encryptionKey));

            responseData.setLoginDevice(SecurityUtils.encrypt(deviceInfo, encryptionKey));
            responseData.setLoginIp(SecurityUtils.encrypt(ipAddress, encryptionKey));
            responseData.setBrowser(SecurityUtils.encrypt(browser, encryptionKey));
            responseData.setOs(SecurityUtils.encrypt(os, encryptionKey));
            responseData.setLastLoginAt(LocalDateTime.now());
            responseData.setSessionId(String.valueOf(session.getId()));
            // 8️⃣ API Response
            ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(
                    true,
                    "Login successful",
                    responseData,
                    now
            );

            return ResponseEntity.ok(apiResponse);

        } catch (LoginFailedException ex) {
            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    ex.getMessage(),
                    ex.getDetails(),
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (AccountLockedException ex) {
            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    "Account locked due to multiple failed login attempts",
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.LOCKED).body(response);
        } catch (RuntimeException e) {
            ApiResponse<Object> response = new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public ResponseEntity<ApiResponse<?>> verifyLoginOtp(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String otp = request.get("otp");

        // Call service method which returns ApiResponse
        ApiResponse<?> response = registrationService.verifyLoginOtp(email, otp);

        // Determine HTTP status based on success and message
        HttpStatus status = HttpStatus.OK; // default

        if (!response.isSuccess()) {
            String msg = response.getMessage().toLowerCase();

            if (msg.contains("locked")) {
                status = HttpStatus.LOCKED; // 423
            } else if (msg.contains("invalid otp") || msg.contains("invalid credentials")) {
                status = HttpStatus.UNAUTHORIZED; // 401
            } else if (msg.contains("not active")) {
                status = HttpStatus.FORBIDDEN; // 403
            } else if (msg.contains("frequently")) {
                status = HttpStatus.TOO_MANY_REQUESTS; // 429
            } else {
                status = HttpStatus.BAD_REQUEST; // fallback
            }
        }

        return ResponseEntity.status(status).body(response);
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest httpRequest) {

        LocalDateTime now = LocalDateTime.now();

        try {
            // 1️⃣ Get Authorization header
            String authHeader = httpRequest.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Missing or invalid Authorization header", null, now));
            }

            // 2️⃣ Extract the refresh token from header
            String refreshToken = authHeader.substring(7); // Remove "Bearer "

            // 3️⃣ Call service to refresh access token
            LoginResponse response = registrationService.refreshAccessToken(refreshToken);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Access token refreshed successfully.",
                            response,
                            now
                    )
            );

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(
                            false,
                            ex.getMessage(),
                            null,
                            now
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
    @PostMapping("/switch-role")
    public ResponseEntity<?> switchRole(
            @RequestBody SwitchRoleRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(
                registrationService.switchRole(request.getRole(), httpRequest)
        );
    }
}