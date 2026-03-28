package com.lms.wishlist_service.controller;

import com.lms.wishlist_service.dto.*;
import com.lms.wishlist_service.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService service;

    /**
     * Professional ADD: Returns 201 CREATED with the full Envelope.
     */
    @PostMapping("/{userId}/add/{courseId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> add(@PathVariable String userId, @PathVariable String courseId) {
        try {
            WishlistResponse data = service.addToWishlist(userId, courseId);

            ApiResponse<WishlistResponse> response = ApiResponse.<WishlistResponse>builder()
                    .status("SUCCESS")
                    .timestamp(LocalDateTime.now())
                    .data(data)
                    .message("Course added to wishlist successfully")
                    .build();

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            ApiResponse<WishlistResponse> errorResponse = ApiResponse.<WishlistResponse>builder()
                    .status("CONFLICT")
                    .timestamp(LocalDateTime.now())
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    /**
     * Professional GET: Returns the exact structure you requested.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<WishlistListResponse>> get(@PathVariable String userId) {
        WishlistListResponse data = service.getWishlist(userId);

        ApiResponse<WishlistListResponse> response = ApiResponse.<WishlistListResponse>builder()
                .status("SUCCESS")
                .timestamp(LocalDateTime.now())
                .data(data)
                .message("Wishlist retrieved successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Professional CONTAINS: Wraps the check in the same Envelope.
     */
    @GetMapping("/{userId}/contains/{courseId}")
    public ResponseEntity<ApiResponse<WishlistCheckResponse>> contains(@PathVariable String userId, @PathVariable String courseId) {
        boolean exists = service.checkExists(userId, courseId);

        WishlistCheckResponse checkData = WishlistCheckResponse.builder()
                .userId(userId)
                .courseId(courseId)
                .isInWishlist(exists)
                .checkedAt(LocalDateTime.now())
                .statusMessage(exists ? "Course is in your wishlist" : "Course is not in your wishlist")
                .build();

        ApiResponse<WishlistCheckResponse> response = ApiResponse.<WishlistCheckResponse>builder()
                .status("SUCCESS")
                .timestamp(LocalDateTime.now())
                .data(checkData)
                .message("Status checked successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Professional MOVE: Returns a success message inside the data block.
     */
    @PostMapping("/{userId}/move-to-cart/{courseId}")
    public ResponseEntity<ApiResponse<String>> moveToCart(@PathVariable String userId, @PathVariable String courseId) {
        service.moveToCart(userId, courseId);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status("SUCCESS")
                .timestamp(LocalDateTime.now())
                .data("COURSE_MOVED")
                .message("Successfully moved to cart")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Professional REMOVE: Standardizes the response even for deletions.
     */
    @DeleteMapping("/{userId}/remove/{courseId}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable String userId, @PathVariable String courseId) {
        service.removeFromWishlist(userId, courseId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("SUCCESS")
                .timestamp(LocalDateTime.now())
                .message("Item removed successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Professional CLEAR: Final cleanup.
     */
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<Void>> clear(@PathVariable String userId) {
        String message = service.clearWishlist(userId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("SUCCESS")
                .timestamp(LocalDateTime.now())
                .data(null) // Make sure this is here!
                .message(message)
                .build();

        return ResponseEntity.ok(response);
    }
}