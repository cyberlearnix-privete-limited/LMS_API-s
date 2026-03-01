package com.user.register.service;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.SessionDto;
import com.user.register.dto.UpdateUserProfileRequest;
import com.user.register.dto.UserProfileResponse;
import com.user.register.entity.User;
import com.user.register.entity.UserSession;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import com.user.register.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final JwtUtil jwtUtil;
    private final String encryptionKey = "1234567890123456"; // your encryption key

    public UserService(UserRepository userRepository,
                       UserSessionRepository sessionRepository,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.jwtUtil = jwtUtil;
    }

    public UserProfileResponse getLoggedInUserProfile(HttpServletRequest request) {
        // 1️⃣ Extract JWT token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        Long userId = Long.parseLong(jwtUtil.validateAccessTokenAndGetUserId(token));

        // 2️⃣ Fetch user from DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3️⃣ Decrypt fields before returning
        String firstName = decrypt(user.getFirstName());
        String lastName = decrypt(user.getLastName());
        String mobile = decrypt(user.getMobile());
        String dob = decrypt(user.getDob());
        String city = decrypt(user.getCity());
        String state = decrypt(user.getState());
        String country = decrypt(user.getCountry());
        String organization = decrypt(user.getOrganization());

        // 4️⃣ Fetch active sessions
        List<SessionDto> activeSessions = sessionRepository.findByUser(user)
                .stream()
                .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(s -> new SessionDto(
                        s.getId(),
                        s.getDeviceInfo(),
                        s.getIpAddress(),
                        s.getCreatedAt(),
                        user.getEmail()
                ))
                .collect(Collectors.toList());

        // 5️⃣ Build profile response
        return new UserProfileResponse(
                user.getId(),
                firstName,
                lastName,
                user.getEmail(),
                mobile,
                dob,
                user.getProfilePhoto(),
                city,
                state,
                country,
                user.getPreferredLanguage(),
                organization,
                user.getSkills(),
                user.getFieldOfStudy(),
                user.getHighestQualification(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin(),
                activeSessions
        );
    }

    // ----------------------------
    // Decrypt helper using your SecurityUtils
    private String decrypt(String value) {
        if (value == null) return null;
        try {
            return SecurityUtils.decrypt(value, encryptionKey);
        } catch (Exception e) {
            // If decryption fails, return original encrypted value as fallback
            return value;
        }
    }

    public UserProfileResponse updateUserProfile(HttpServletRequest request, UpdateUserProfileRequest updateRequest) {
        // 1️⃣ Extract userId from JWT
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        Long userId = Long.parseLong(jwtUtil.validateAccessTokenAndGetUserId(token));

        // 2️⃣ Fetch user from DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            // 3️⃣ Update fields if present and encrypt
            if (updateRequest.getFirstName() != null)
                user.setFirstName(SecurityUtils.encrypt(updateRequest.getFirstName(), encryptionKey));
            if (updateRequest.getLastName() != null)
                user.setLastName(SecurityUtils.encrypt(updateRequest.getLastName(), encryptionKey));
            if (updateRequest.getMobile() != null)
                user.setMobile(SecurityUtils.encrypt(updateRequest.getMobile(), encryptionKey));
            if (updateRequest.getDob() != null)
                user.setDob(SecurityUtils.encrypt(updateRequest.getDob(), encryptionKey));
            if (updateRequest.getCity() != null)
                user.setCity(SecurityUtils.encrypt(updateRequest.getCity(), encryptionKey));
            if (updateRequest.getState() != null)
                user.setState(SecurityUtils.encrypt(updateRequest.getState(), encryptionKey));
            if (updateRequest.getCountry() != null)
                user.setCountry(SecurityUtils.encrypt(updateRequest.getCountry(), encryptionKey));
            if (updateRequest.getOrganization() != null)
                user.setOrganization(SecurityUtils.encrypt(updateRequest.getOrganization(), encryptionKey));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting user fields: " + e.getMessage(), e);
        }

        // 4️⃣ Update other fields
        if (updateRequest.getPreferredLanguage() != null)
            user.setPreferredLanguage(updateRequest.getPreferredLanguage());
        if (updateRequest.getProfilePhoto() != null) user.setProfilePhoto(updateRequest.getProfilePhoto());
        if (updateRequest.getSkills() != null) user.setSkills(updateRequest.getSkills());
        if (updateRequest.getFieldOfStudy() != null) user.setFieldOfStudy(updateRequest.getFieldOfStudy());
        if (updateRequest.getHighestQualification() != null)
            user.setHighestQualification(updateRequest.getHighestQualification());

        // 5️⃣ Save user
        userRepository.save(user);

        // 6️⃣ Build simplified response (decrypted fields only)
        try {
            return new UserProfileResponse(
                    user.getId(),
                    SecurityUtils.decrypt(user.getFirstName(), encryptionKey),
                    SecurityUtils.decrypt(user.getLastName(), encryptionKey),
                    user.getEmail(),
                    SecurityUtils.decrypt(user.getMobile(), encryptionKey),
                    SecurityUtils.decrypt(user.getDob(), encryptionKey),
                    user.getProfilePhoto(),
                    SecurityUtils.decrypt(user.getCity(), encryptionKey),
                    SecurityUtils.decrypt(user.getState(), encryptionKey),
                    SecurityUtils.decrypt(user.getCountry(), encryptionKey),
                    user.getPreferredLanguage(),
                    SecurityUtils.decrypt(user.getOrganization(), encryptionKey),
                    user.getSkills(),
                    user.getFieldOfStudy(),
                    user.getHighestQualification(),
                    user.getRole().name(),
                    user.getStatus().name(),
                    null, // createdAt
                    null, // updatedAt
                    null, // lastLogin
                    null  // activeSessions

            );
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting user fields: " + e.getMessage(), e);
        }
    }

    public UserProfileResponse uploadProfilePhoto(HttpServletRequest request, MultipartFile file) {
        // 1️⃣ Extract userId from JWT
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        Long userId = Long.parseLong(jwtUtil.validateAccessTokenAndGetUserId(token));

        // 2️⃣ Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3️⃣ Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("No file uploaded");
        }
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new RuntimeException("File size exceeds 5MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new RuntimeException("Only JPG, PNG, or WEBP files are allowed");
        }
        try {
            // 4️⃣ Resize to 512x512
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage resizedImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, 512, 512, null);
            g.dispose();
            // 5️⃣ Save file
            String extension = contentType.equals("image/jpeg") ? "jpg" : contentType.split("/")[1];
            String filename = UUID.randomUUID().toString() + "." + extension;
            String uploadDir = "uploads"; // make sure this folder exists
            Files.createDirectories(Paths.get(uploadDir));
            File outputFile = new File(uploadDir + File.separator + filename);
            ImageIO.write(resizedImage, extension, outputFile);

            // 6️⃣ Update user
            user.setProfilePhoto("/uploads/" + filename);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            // 7️⃣ Fetch active sessions
            List<SessionDto> activeSessions = sessionRepository.findByUser(user)
                    .stream()
                    .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))
                    .map(s -> new SessionDto(
                            s.getId(),
                            s.getDeviceInfo(),
                            s.getIpAddress(),
                            s.getCreatedAt(),
                            user.getEmail()
                    ))
                    .collect(Collectors.toList());
            // 8️⃣ Return simplified response
            return new UserProfileResponse(
                    user.getId(),
                    decrypt(user.getFirstName()),
                    decrypt(user.getLastName()),
                    user.getEmail(),
                    decrypt(user.getMobile()),
                    decrypt(user.getDob()),
                    user.getProfilePhoto(),
                    decrypt(user.getCity()),
                    decrypt(user.getState()),
                    decrypt(user.getCountry()),
                    user.getPreferredLanguage(),
                    decrypt(user.getOrganization()),
                    user.getSkills(),
                    user.getFieldOfStudy(),
                    user.getHighestQualification(),
                    user.getRole().name(),
                    user.getStatus().name(),
                    null, // createdAt
                    null, // updatedAt
                    null, // lastLogin
                    null  // activeSessions
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to process uploaded image: " + e.getMessage(), e);
        }
    }
    public ApiResponse<UserProfileResponse> softDeleteUser(HttpServletRequest request) {
        // 1️⃣ Extract JWT
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        Long userId = Long.parseLong(jwtUtil.validateAccessTokenAndGetUserId(token));

        // 2️⃣ Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3️⃣ Soft delete: mark user as SUSPENDED (DB allowed value)
        user.setStatus(User.Status.SUSPENDED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 4️⃣ Build detailed UserProfileResponse without activeSessions and lastLogin
        UserProfileResponse profile = new UserProfileResponse(
                user.getId(),
                decrypt(user.getFirstName()),
                decrypt(user.getLastName()),
                user.getEmail(),
                decrypt(user.getMobile()),
                decrypt(user.getDob()),
                user.getProfilePhoto(),
                decrypt(user.getCity()),
                decrypt(user.getState()),
                decrypt(user.getCountry()),
                user.getPreferredLanguage(),
                decrypt(user.getOrganization()),
                user.getSkills(),
                user.getFieldOfStudy(),
                user.getHighestQualification(),
                user.getRole().name(),
                user.getStatus().name(), // now SUSPENDED
                user.getCreatedAt(),
                user.getUpdatedAt(),
                null,   // lastLogin removed
                null    // activeSessions removed
        );

        // 5️⃣ Return detailed ApiResponse
        return new ApiResponse<>(
                true,
                "Account has been deactivated successfully",
                profile,
                LocalDateTime.now()
        );
    }


}