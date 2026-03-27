package com.lms.cart_service.exception;

import com.lms.cart_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // PRD Section 7.1: Course not found -> 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handle404(ResourceNotFoundException e) {
        return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    // PRD Section 7.4: Coupon invalid -> 422
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handle422(IllegalArgumentException e) {
        return buildResponse(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // PRD Section 6 & 7.3: Data Isolation Violation / Unauthorized -> 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handle403(AccessDeniedException e) {
        return buildResponse("Access Denied: " + e.getMessage(), HttpStatus.FORBIDDEN);
    }

    // PRD Section 7.2: Cart empty / Logic Errors -> 400
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handle400(RuntimeException e) {
        return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Helper method to use the static error builder from ApiResponse DTO.
     * This keeps the timestamp and success flag logic centralized.
     */
    private ResponseEntity<ApiResponse<Object>> buildResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(ApiResponse.error(message), status);
    }
}