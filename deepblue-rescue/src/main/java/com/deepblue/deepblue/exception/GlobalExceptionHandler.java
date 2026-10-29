package com.deepblue.deepblue.exception;

import com.deepblue.deepblue.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //  404 (Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                Map.of()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    //  409 (Conflict)
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                Map.of()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
    //400 Bad Request
    //atrapar los errores cuando el usuario envíe datos incorrectos o incompletos
    // (por ejemplo, si deja vacío el campo animalCode que es obligatorio).
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(org.springframework.web.bind.
                                                                      MethodArgumentNotValidException ex) {

        Map<String, String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(
                        java.util.stream.Collectors.toMap(
                                org.springframework.validation.FieldError::getField,
                                org.springframework.validation.FieldError::getDefaultMessage,
                                (first, second) -> first
                        )
                );

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Request validation failed",
                details
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    //400 Bad Request
    //error que ocurre cuando el cliente envía un JSON mal escrito (por ejemplo, le falta una coma o una llave) o cuando envía un valor
    // que no existe en nuestros Enums (por ejemplo, mandar "status": "FLYING" en lugar de IN_REHABILITATION).
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Malformed or invalid JSON request",
                Map.of("body", "Check JSON syntax and enum values")
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    //400 Bad Request
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, String> details = Map.of(
                ex.getName(),
                "Invalid value: " + String.valueOf(ex.getValue())
        );

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "Invalid request parameter",
                details
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    // 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "An unexpected error occurred",
                Map.of()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}