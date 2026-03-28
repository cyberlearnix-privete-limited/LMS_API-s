package com.lms.cart_service.exception;

// Professional Note: We remove @ResponseStatus because the
// GlobalExceptionHandler provides more granular control.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}