package com.user.register.exception;


import java.util.Map;

public class LoginFailedException extends RuntimeException {

    private final Map<String, Object> details;

    public LoginFailedException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }


}