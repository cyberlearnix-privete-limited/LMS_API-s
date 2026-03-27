package com.user.register.exception;

public class InvalidOtpException extends RuntimeException {
    private final int remainingAttempts;
    private final long secondsUntilExpiry;

    public InvalidOtpException(String message, int remainingAttempts, long secondsUntilExpiry) {
        super(message);
        this.remainingAttempts = remainingAttempts;
        this.secondsUntilExpiry = secondsUntilExpiry;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public long getSecondsUntilExpiry() {
        return secondsUntilExpiry;
    }
}
