package com.example.treasury.api;

import java.time.LocalDateTime;

public class ApiError {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final String message;

    public ApiError(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
