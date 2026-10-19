package com.example.supportticket.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<String> messages = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            messages.add(fieldError.getDefaultMessage());
        }
        String message = String.join(", ", messages);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        String message = "Request body is missing or is not valid JSON";
        if (ex.getCause() instanceof InvalidFormatException cause && isEnum(cause.getTargetType())) {
            message = "Invalid value '" + cause.getValue() + "'" + allowedValues(cause.getTargetType());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
        if (isEnum(ex.getRequiredType())) {
            message = message + allowedValues(ex.getRequiredType());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSort(PropertyReferenceException ex,
                                                           HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Cannot sort by unknown field '" + ex.getPropertyName() + "'",
                request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoEndpoint(NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND,
                "No endpoint found for " + request.getMethod() + " " + request.getRequestURI(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest request) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED,
                "Method " + ex.getMethod() + " is not allowed for this endpoint", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest request) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type must be application/json", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.",
                request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message,
                                                        HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), status.getReasonPhrase(), message,
                request.getRequestURI());
        return new ResponseEntity<>(errorResponse, status);
    }

    private boolean isEnum(Class<?> type) {
        return type != null && type.isEnum();
    }

    private String allowedValues(Class<?> type) {
        return ". Allowed values: " + Arrays.toString(type.getEnumConstants());
    }
}