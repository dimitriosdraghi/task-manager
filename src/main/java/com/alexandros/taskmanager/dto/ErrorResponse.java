package com.alexandros.taskmanager.dto;

import java.time.Instant;

/** Common body for all error responses. */
public record ErrorResponse(int status, String error, String message, Instant timestamp) {

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, Instant.now());
    }
}
