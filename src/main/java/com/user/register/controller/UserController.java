package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.UpdateUserProfileRequest;
import com.user.register.dto.UserProfileResponse;
import com.user.register.security.JwtUtil;
import com.user.register.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
                    .body(new ApiResponse<>(false, e.getMessage(), null));
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

}