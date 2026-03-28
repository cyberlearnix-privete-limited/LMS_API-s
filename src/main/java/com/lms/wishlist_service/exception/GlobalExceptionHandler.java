package com.lms.wishlist_service.exception;

import com.lms.wishlist_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles your custom WishlistException (e.g., Course already exists)
     */
    @ExceptionHandler(WishlistException.class)
    public ResponseEntity<ApiResponse<Void>> handleWishlistException(WishlistException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("ERROR")
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, ex.getStatus());
    }

    /**
     * Fallback for unexpected errors (NullPointer, Database down, etc.)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .message("An unexpected error occurred: " + ex.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}