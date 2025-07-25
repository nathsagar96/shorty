package com.shorty.common.exception;

import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
    ErrorResponse error =
        new ErrorResponse("VALIDATION_ERROR", e.getMessage(), LocalDateTime.now());
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException e) {
    ErrorResponse error =
        new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ErrorResponse> handleJwtException(JwtException e) {
    ErrorResponse error;
    if (e instanceof io.jsonwebtoken.security.SignatureException) {
      error = new ErrorResponse("INVALID_JWT_SIGNATURE", e.getMessage(), LocalDateTime.now());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    } else if (e instanceof io.jsonwebtoken.MalformedJwtException) {
      error = new ErrorResponse("MALFORMED_JWT", e.getMessage(), LocalDateTime.now());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    } else {
      error = new ErrorResponse("INVALID_JWT_TOKEN", e.getMessage(), LocalDateTime.now());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
  }
}
