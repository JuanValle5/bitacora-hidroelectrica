package com.hidroelectrica.bitacora.dto.response;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldErrorDetail> errors
) {
    public record FieldErrorDetail(
        String field,
        Object rejectedValue,
        String message
    ) {}

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, null);
    }

    public static ApiErrorResponse of(int status, String error, String message, String path, List<FieldErrorDetail> errors) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, errors);
    }
}
