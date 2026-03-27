package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.UpdateUserProfileRequest;
import com.user.register.dto.UpdateUserStatusRequest;
import com.user.register.dto.UserProfileResponse;
import com.user.register.entity.User;
import com.user.register.security.JwtUtil;
import com.user.register.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;  // ✅ Add this
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;  // ✅ now properly initialized


    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(HttpServletRequest request) {
        try {
            UserProfileResponse profile = userService.getLoggedInUserProfile(request);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User profile fetched successfully", profile, LocalDateTime.now())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, "Missing or invalid Authorization header", null));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            HttpServletRequest request,
            @RequestBody UpdateUserProfileRequest updateRequest
    ) {
        try {
            UserProfileResponse updatedProfile = userService.updateUserProfile(request, updateRequest);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Profile updated successfully", updatedProfile, LocalDateTime.now())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    // ---------------- POST /users/photo ----------------
    @PostMapping("/photo")
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadProfilePhoto(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            UserProfileResponse updatedProfile = userService.uploadProfilePhoto(request, file);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Profile photo updated successfully", updatedProfile, LocalDateTime.now())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> deleteAccount(HttpServletRequest request) {
        try {
            ApiResponse<UserProfileResponse> response = userService.softDeleteUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }


    @PostMapping("/login/social")
    public ResponseEntity<ApiResponse<?>> socialLogin(@RequestBody Map<String, String> requestBody) {
        try {
            String email = requestBody.get("email");
            String provider = requestBody.get("provider"); // GOOGLE, GITHUB, LINKEDIN

            // Login or register via social login
            User user = userService.socialLogin(email, provider);

            // Generate JWT token using user ID
            String token = jwtUtil.generateAccessToken(user.getId().toString(), user.getRole().name());

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Login successful",
                            Map.of(
                                    "email", user.getEmail(),
                                    "token", token,
                                    "provider", provider
                            ),
                            LocalDateTime.now()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllUsers() {
        try {
            var users = userService.getAllUsers(); // 👈 make sure this exists

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Users fetched successfully",
                            users,
                            LocalDateTime.now()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(
                            false,
                            e.getMessage(),
                            null,
                            LocalDateTime.now()
                    ));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable UUID id) {
        try {
            UserProfileResponse user = userService.getUserById(id);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User fetched successfully", user, LocalDateTime.now())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserStatus(
            @PathVariable UUID id,
            @RequestBody UpdateUserStatusRequest request
    ) {
        try {

            if (request.getStatus() == null || request.getStatus().isBlank()) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(false, "Status is required", null, LocalDateTime.now())
                );
            }

            UserProfileResponse updated =
                    userService.updateUserStatus(id, request.getStatus());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Status updated successfully", updated, LocalDateTime.now())
            );

        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> deleteUser(@PathVariable UUID id) {

        UserProfileResponse user = userService.getUserById(id); // fetch before delete

        userService.deleteUserById(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "User deleted successfully", user, LocalDateTime.now())
        );
    }
}