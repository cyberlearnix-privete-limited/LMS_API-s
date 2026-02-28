package com.user.register.exception;

public class OtpCooldownException extends RuntimeException {

    private final long remainingSeconds;

    public OtpCooldownException(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}