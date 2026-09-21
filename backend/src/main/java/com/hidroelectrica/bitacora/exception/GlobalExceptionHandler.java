package com.hidroelectrica.bitacora.exception;

import com.hidroelectrica.bitacora.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorResponse.FieldErrorDetail> details = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.add(new ApiErrorResponse.FieldErrorDetail(
                fe.getField(),
                fe.getRejectedValue(),
                fe.getDefaultMessage()
            ));
        }

        ApiErrorResponse response = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            "La solicitud contiene campos con validación fallida",
            request.getRequestURI(),
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
