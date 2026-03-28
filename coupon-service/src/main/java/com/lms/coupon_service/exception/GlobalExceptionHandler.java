package com.lms.coupon_service.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handling for the Coupon Service.
 * Ensures all errors follow a consistent JSON format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles specific business logic errors thrown from the Service layer.
     * Triggered by: Validation failures, limit reached, or invalid assignments.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessLogic(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("success", false);
        response.put("error", "Validation Error");
        response.put("message", ex.getMessage()); // This will say "Coupon not found or already deleted"

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    /**
     * Handles Database Integrity issues.
     * Triggered by: Duplicate Coupon Codes (Unique constraint violation).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateKeyException(DataIntegrityViolationException ex) {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("success", false);
        response.put("error", "Conflict");
        response.put("message", "A coupon with this code already exists.");

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Catch-all for all other unexpected exceptions.
     * Ensures the API always returns a JSON body rather than an empty response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("success", false);
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred: " + ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
