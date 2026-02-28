package com.user.register.service;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.ForgotPasswordResponseData;
import com.user.register.dto.LoginRequest;
import com.user.register.dto.LoginResponse;
import com.user.register.entity.User;
import com.user.register.entity.OTPCode;
import com.user.register.entity.AuditLog;
import com.user.register.exception.InvalidOtpException;
import com.user.register.exception.LoginFailedException;
import com.user.register.repository.UserRepository;
import com.user.register.repository.OTPCodeRepository;
import com.user.register.repository.AuditLogRepository;
import com.user.register.security.JwtUtil;
import com.user.register.util.SecurityUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional

public class RegistrationService {

    private final UserRepository userRepository;
    private final OTPCodeRepository otpRepository;
    private final AuditLogRepository auditLogRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder; // inject bean
    private static final int MAX_FAILED_LOGIN = 5;
    private final JwtUtil jwtUtil; // <-- inject JwtUtil
    private final TokenBlacklistService blacklistService;
    @Value("${app.encryption.key:1234567890123456}")
    private String encryptionKey;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final int MAX_OTP_ATTEMPTS = 5;
    private Object newAccessToken;
    private String accessToken;

    public User register(User user) throws Exception {
        // ================= RATE LIMIT CHECK =================
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRegistrations = userRepository.countByEmailOrMobileAndCreatedAtAfter(
                user.getEmail(),
                SecurityUtils.encrypt(user.getMobile(), encryptionKey),
                oneHourAgo
        );

        if (recentRegistrations >= 3) {
            throw new RuntimeException("Rate limit exceeded: You can only register 3 times per hour.");
        }
// 2️⃣ Check password strength
        // ===============================================
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            throw new RuntimeException("Password and Confirm Password do not match");
        }

        if (!isPasswordStrong(user.getPassword())) {
            throw new RuntimeException("Password is too weak. It must be at least 8 characters, " +
                    "contain uppercase, lowercase, number, and special character.");
        }

        // <<< END OF PASSWORD CHECKS >>>
        // <<< ADD MOBILE NUMBER CHECK RIGHT AFTER PASSWORD CHECKS >>>
        String encryptedMobile = SecurityUtils.encrypt(user.getMobile(), encryptionKey);
        Optional<User> userWithMobile = userRepository.findByMobile(encryptedMobile);
        if (userWithMobile.isPresent() && userWithMobile.get().getStatus() == User.Status.ACTIVE) {
            throw new RuntimeException("Mobile number already registered");
        }
        Optional<User> existingUserOpt = userRepository.findByEmail(user.getEmail());

        // ================= EXISTING USER =================
        if (existingUserOpt.isPresent()) {

            User existingUser = existingUserOpt.get();

            // If already ACTIVE → block
            if (existingUser.getStatus() == User.Status.ACTIVE) {
                throw new RuntimeException("Email already registered");
            }

            // If PENDING_VERIFICATION → UPDATE profile photo + resend OTP
            if (existingUser.getStatus() == User.Status.PENDING_VERIFICATION) {

                // ✅ Update profile photo if provided
                if (user.getProfilePhoto() != null && !user.getProfilePhoto().isBlank()) {
                    existingUser.setProfilePhoto(user.getProfilePhoto());
                }

                userRepository.save(existingUser);

                String otp = generateOTP();
                System.out.println("Resent OTP for testing: " + otp);

                OTPCode otpCode = OTPCode.builder()
                        .user(existingUser)
                        .otp(otp)
                        .type("registration")
                        .expiresAt(LocalDateTime.now().plusMinutes(5))
                        .attempts(0)
                        .createdAt(LocalDateTime.now())
                        .build();

                otpRepository.save(otpCode);

                sendOtpEmail(existingUser.getEmail(), otp, "Password Reset OTP");

                return existingUser;
            }
        }

        // ================= NEW USER =================

        // Encrypt sensitive fields
        user.setFirstName(SecurityUtils.encrypt(user.getFirstName(), encryptionKey));
        user.setLastName(SecurityUtils.encrypt(user.getLastName(), encryptionKey));
        user.setMobile(SecurityUtils.encrypt(user.getMobile(), encryptionKey));
        user.setDob(SecurityUtils.encrypt(user.getDob(), encryptionKey));
        user.setCity(SecurityUtils.encrypt(user.getCity(), encryptionKey));
        user.setState(SecurityUtils.encrypt(user.getState(), encryptionKey));
        user.setCountry(SecurityUtils.encrypt(user.getCountry(), encryptionKey));

        if (user.getOrganization() != null) {
            user.setOrganization(SecurityUtils.encrypt(user.getOrganization(), encryptionKey));
        }

        // Hash password
        user.setPassword(SecurityUtils.hashPassword(user.getPassword()));

        user.setStatus(User.Status.PENDING_VERIFICATION);
        user.setRole(User.Role.STUDENT);
        user.setIsInstructorApproved(false);

        // ✅ profilePhoto already set from request (no need to re-set)

        User savedUser = userRepository.save(user);

        // Generate OTP
        String otp = generateOTP();
        System.out.println("Generated OTP for testing: " + otp);

        OTPCode otpCode = OTPCode.builder()
                .user(savedUser)
                .otp(otp)
                .type("registration")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        otpRepository.save(otpCode);

        // Audit log
        auditLogRepository.save(AuditLog.builder()
                .user(savedUser)
                .action("REGISTER")
                .ipAddress("N/A")
                .createdAt(LocalDateTime.now())
                .build());

        sendOtpEmail(savedUser.getEmail(), otp, "Password Reset OTP");

        return savedUser;
    }

    /**
     * Simple password strength check
     */
    private boolean isPasswordStrong(String password) {
        if (password.length() < 8) return false;
        if (!password.matches(".*[A-Z].*")) return false;
        if (!password.matches(".*[a-z].*")) return false;
        if (!password.matches(".*\\d.*")) return false;
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) return false;
        return true;
    }

    private void sendOtpEmail(String email, String otp, String passwordResetOtp) {
        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);   // ✅ use property
            message.setTo(email);
            message.setSubject("Registration OTP Verification");
            message.setText(
                    "Your OTP is: " + otp +
                            "\n\nIt is valid for 5 minutes." +
                            "\n\nIf you did not request this, ignore this email."
            );

            mailSender.send(message);

            System.out.println("OTP email sent successfully to: " + email);

        } catch (Exception e) {
            System.out.println("Email sending failed");
            e.printStackTrace();
        }
    }

    /**
     * Upload and process profile photo
     * Rules:
     * - Max 5MB
     * - JPG / PNG / WEBP only
     * - Resize to 512x512
     */
    public String uploadProfilePhoto(MultipartFile file) throws Exception {

        // 1️⃣ Check empty
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // 2️⃣ Validate size (5MB)
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size must be less than 5MB");
        }

        // 3️⃣ Validate content type
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/webp"))) {

            throw new RuntimeException("Only JPG, PNG, WEBP formats allowed");
        }

        // 4️⃣ Read image
        java.awt.image.BufferedImage originalImage =
                javax.imageio.ImageIO.read(file.getInputStream());

        if (originalImage == null) {
            throw new RuntimeException("Invalid image file");
        }

        // 5️⃣ Resize to 512x512
        java.awt.image.BufferedImage resizedImage =
                new java.awt.image.BufferedImage(512, 512, java.awt.image.BufferedImage.TYPE_INT_RGB);

        java.awt.Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, 512, 512, null);
        g.dispose();

        // 6️⃣ Create uploads folder
        String uploadDir = "uploads/";
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 7️⃣ Generate file name
        String extension = contentType.equals("image/png") ? "png"
                : contentType.equals("image/webp") ? "webp"
                : "jpg";

        String fileName = java.util.UUID.randomUUID() + "." + extension;
        java.io.File outputFile = new java.io.File(uploadDir + fileName);

        // 8️⃣ Save image
        javax.imageio.ImageIO.write(
                resizedImage,
                extension.equals("jpg") ? "jpeg" : extension,
                outputFile
        );

        // 9️⃣ Return public URL
        return "http://localhost:8080/uploads/" + fileName;
    }

    public Map<String, Object> verifyOTP(String email, String otp) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == User.Status.LOCKED)
            throw new RuntimeException("Account locked due to failed OTP attempts");

        if (user.getStatus() == User.Status.SUSPENDED)
            throw new RuntimeException("Account suspended");

        // Get latest registration OTP
        OTPCode code = (OTPCode) otpRepository
                .findTopByUserAndTypeOrderByCreatedAtDesc(user, "registration")
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        // Expiry check
        if (code.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("OTP expired");

        // Increment attempts
        int attempts = code.getAttempts() + 1;
        code.setAttempts(attempts);
        otpRepository.save(code);

        // Lock after 5 failed attempts
        if (attempts > 5) {
            user.setStatus(User.Status.LOCKED);
            userRepository.save(user);
            throw new RuntimeException("Too many failed OTP attempts. Account locked.");
        }

        // ❌ OTP mismatch → throw exception instead of returning Map
        if (!code.getOtp().equals(otp)) {
            long secondsToExpire = Duration.between(LocalDateTime.now(), code.getExpiresAt()).getSeconds();
            throw new InvalidOtpException("Invalid OTP", 5 - attempts, secondsToExpire);
        }

        // ✅ OTP correct → success
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);
        otpRepository.delete(code);

        auditLogRepository.save(AuditLog.builder()
                .user(user)
                .action("VERIFY_OTP_SUCCESS")
                .ipAddress("N/A")
                .createdAt(LocalDateTime.now())
                .build());

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", "Email verified successfully");
        successResponse.put("data", user);
        successResponse.put("timestamp", LocalDateTime.now());

        return successResponse;
    }

    /**
     * Fetch user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Generate 6-digit OTP
     */
    private String generateOTP() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    public LoginResponse loginWithPassword(LoginRequest request) {
        // 1️⃣ Fetch user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // 2️⃣ Check account status
        if (user.getStatus() == User.Status.PENDING_VERIFICATION) {
            throw new RuntimeException("Email not verified");
        }
        if (user.getStatus() == User.Status.LOCKED) {
            throw new RuntimeException("Account locked due to too many failed login attempts");
        }
        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new RuntimeException("Account suspended by admin");
        }

        // 3️⃣ Check instructor approval
        if (user.getRole() == User.Role.INSTRUCTOR && !user.getIsInstructorApproved()) {
            throw new RuntimeException("Instructor account not approved yet");
        }

        // 4️⃣ Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int failed = user.getFailedLoginAttempts() == null ? 1 : user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failed);

            if (failed >= MAX_FAILED_LOGIN) {
                user.setStatus(User.Status.LOCKED);
            }

            userRepository.save(user);

            auditLogRepository.save(AuditLog.builder()
                    .user(user)
                    .action("LOGIN_FAILED")
                    .ipAddress("N/A")
                    .createdAt(LocalDateTime.now())
                    .build());

            Map<String, Object> errorData = Map.of(
                    "remainingAttempts", MAX_FAILED_LOGIN - failed,
                    "accountStatus", user.getStatus().name()
            );

            throw new LoginFailedException("Invalid credentials", errorData);
        }

        // 5️⃣ Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);

        // ✅ Ensure timestamps are set for existing users
        if (user.getCreatedAt() == null) user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Optional: set last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user); // triggers update for updatedAt & lastLoginAt

        // 6️⃣ Generate tokens
        String accessToken = jwtUtil.generateAccessToken(String.valueOf(user.getId())); // 15 mins
        String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(user.getId())); // 30 days

        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(15);
        LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(30);

        auditLogRepository.save(AuditLog.builder()
                .user(user)
                .action("LOGIN_SUCCESS")
                .ipAddress("N/A")
                .createdAt(LocalDateTime.now())
                .build());
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setMobile(user.getMobile());
        response.setDob(user.getDob());
        response.setProfilePhoto(user.getProfilePhoto());
        response.setCity(user.getCity());
        response.setState(user.getState());
        response.setCountry(user.getCountry());
        response.setOrganization(user.getOrganization());
        response.setPreferredLanguage(user.getPreferredLanguage());
        response.setSkills(user.getSkills());
        response.setFieldOfStudy(user.getFieldOfStudy());
        response.setHighestQualification(user.getHighestQualification());
        response.setStatus(user.getStatus().name());
        response.setRole(user.getRole().name());
        response.setFailedLoginAttempts(user.getFailedLoginAttempts());
        response.setIsInstructorApproved(user.getIsInstructorApproved());
        response.setCreatedAt(user.getCreatedAt()); // entity field
        response.setUpdatedAt(user.getUpdatedAt()); // entity field
        response.setLastLoginAt(LocalDateTime.now());
        response.setLoginDevice("Web");
        response.setLoginIp("N/A");
        response.setSessionId(UUID.randomUUID().toString());
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresInSeconds(15 * 60);
        response.setAccessTokenExpiresAt(LocalDateTime.now().plusMinutes(15));
        response.setRefreshTokenExpiresAt(LocalDateTime.now().plusDays(30));
        return response;
    }

    /**
     * Request OTP for login
     * Used by POST /auth/login/otp/request
     */
    public ApiResponse<Map<String, Object>> sendLoginOtp(String email) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return new ApiResponse<>(
                    false,
                    "User not found",
                    null,
                    LocalDateTime.now()
            );
        }

        User user = userOptional.get();

        if (user.getStatus() != User.Status.ACTIVE) {
            return new ApiResponse<>(
                    false,
                    "Account not active",
                    null,
                    LocalDateTime.now()
            );
        }

        Optional<OTPCode> recentOtpOptional =
                otpRepository.findTopByUserAndTypeOrderByCreatedAtDesc(user, "login");

        if (recentOtpOptional.isPresent()) {
            OTPCode recentOtp = recentOtpOptional.get();

            if (recentOtp.getCreatedAt()
                    .plusSeconds(30)
                    .isAfter(LocalDateTime.now())) {

                return new ApiResponse<>(
                        false,
                        "OTP requested too frequently. Please wait 30 seconds.",
                        null,
                        LocalDateTime.now()
                );
            }
        }
        String otp = generateOTP();

        OTPCode otpCode = OTPCode.builder()
                .user(user)
                .otp(otp)
                .type("login")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        otpRepository.save(otpCode);

        sendOtpEmail(user.getEmail(), otp, "Password Reset OTP");

        Map<String, Object> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("otpType", "login");
        data.put("expiresAt", otpCode.getExpiresAt());
        data.put("validForMinutes", 5);
        data.put("cooldownSeconds", 30);
        return new ApiResponse<>(
                true,
                "Login OTP sent successfully to registered email.",
                data,
                LocalDateTime.now()
        );
    }

    public LoginResponse verifyLoginOtp(String email, String otp) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (user.getStatus() != User.Status.ACTIVE) {
            throw new RuntimeException("Account not active");
        }

        OTPCode code = otpRepository
                .findTopByUserAndTypeOrderByCreatedAtDesc(user, "login")
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(code);
            throw new RuntimeException("OTP expired");
        }

        if (!code.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        otpRepository.delete(code);

        // ✅ Generate tokens
        String accessToken = jwtUtil.generateAccessToken(String.valueOf(user.getId()));   // 15 mins
        String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(user.getId())); // 30 days

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setAccessTokenExpiresInMinutes(15L);
        response.setRefreshTokenExpiresInDays(30L);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setLoginTime(LocalDateTime.now());
        response.setExpiresInSeconds(900); // 15 minutes

        return response;
    }

    public LoginResponse refreshAccessToken(String refreshToken) {

        // 1️⃣ Validate refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // 2️⃣ Check token type
        String tokenType = jwtUtil.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("Invalid token type");
        }

        // 3️⃣ Extract userId
        String userId = jwtUtil.extractUserId(refreshToken);

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != User.Status.ACTIVE) {
            throw new RuntimeException("User account not active");
        }

        // 4️⃣ Generate new access token (15 minutes)
        String newAccessToken = jwtUtil.generateAccessToken(userId);

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(refreshToken); // reuse same refresh token
        response.setExpiresInSeconds(15 * 60); // 900 seconds

        return response;
    }

    // ===================== FORGOT PASSWORD =====================
    public ForgotPasswordResponseData sendResetOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != User.Status.ACTIVE) {
            throw new RuntimeException("Account not active");
        }

        // Remove old OTPs
        otpRepository.deleteByUserAndType(user, "password_reset");

        // Generate OTP
        String otp = generateOTP();

        OTPCode otpCode = OTPCode.builder()
                .user(user)
                .otp(otp)
                .type("password_reset")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        otpRepository.save(otpCode);

        // Send OTP email
        sendOtpEmail(user.getEmail(), otp, "Password Reset OTP");

        return new ForgotPasswordResponseData(
                user.getEmail(),
                6, // OTP length
                otpCode.getExpiresAt(),
                "OTP sent to email. Use it within 10 minutes."
        );
    }

    // ===================== RESET PASSWORD =====================
    public Map<String, Object> resetPasswordWithOtp(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OTPCode otpCode = otpRepository.findTopByUserAndTypeOrderByCreatedAtDesc(user, "password_reset")
                .orElseThrow(() -> new RuntimeException("No OTP found. Request a new OTP."));

        // Expiry check
        if (otpCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpCode);
            throw new RuntimeException("OTP expired. Request a new OTP.");
        }

        // Increment attempts on mismatch
        if (!otpCode.getOtp().equals(otp)) {
            int attempts = otpCode.getAttempts() + 1;
            otpCode.setAttempts(attempts);
            otpRepository.save(otpCode);

            if (attempts >= 5) {
                user.setStatus(User.Status.LOCKED);
                userRepository.save(user);
                throw new RuntimeException("Too many failed OTP attempts. Account locked.");
            }

            long secondsLeft = Duration.between(LocalDateTime.now(), otpCode.getExpiresAt()).getSeconds();

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("remainingAttempts", 5 - attempts);
            errorData.put("expiresInSeconds", secondsLeft);

            throw new InvalidOtpException("Invalid OTP", 5 - attempts, secondsLeft);
        }

        // ✅ OTP correct → reset password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpRepository.delete(otpCode); // Remove OTP after success

        Map<String, Object> successData = new HashMap<>();
        successData.put("email", user.getEmail());
        successData.put("passwordChangedAt", LocalDateTime.now());
        successData.put("message", "Password reset successfully.");

        return successData;
    }
    public Map<String, Object> logoutCurrentDevice(String accessToken, String ipAddress, String deviceInfo) {
        // 1️⃣ Validate token & extract userId
        String userId = jwtUtil.validateAccessTokenAndGetUserId(accessToken);

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        blacklistService.blacklistToken(accessToken);
        // 2️⃣ Add token to blacklist

        // 3️⃣ Save audit log
        auditLogRepository.save(AuditLog.builder()
                .user(user)
                .action("LOGOUT")
                .ipAddress(ipAddress)
                .device(deviceInfo)
                .createdAt(LocalDateTime.now())
                .build());

        // 4️⃣ Prepare response
        Map<String, Object> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("logoutDevice", deviceInfo);
        data.put("logoutIp", ipAddress);
        data.put("logoutTime", LocalDateTime.now());
        data.put("sessionTerminated", true);

        return data;
    }}