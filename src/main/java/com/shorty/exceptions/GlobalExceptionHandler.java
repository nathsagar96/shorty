package com.shorty.exceptions;

import java.net.URI;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERRORS_BASE_URL = "https://api.shorty.com/errors";

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = AliasAlreadyExistsException.class)
    public ProblemDetail handleAliasAlreadyExists(AliasAlreadyExistsException exception) {
        log.warn("Alias conflict: {}", exception.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Alias Already Exists");
        problemDetail.setType(URI.create(ERRORS_BASE_URL + "/alias-conflict"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ResponseStatus(HttpStatus.GONE)
    @ExceptionHandler(value = UrlExpiredException.class)
    public ProblemDetail handleUrlExpired(UrlExpiredException exception) {
        log.warn("Expired URL accessed: {}", exception.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.GONE, exception.getMessage());
        problemDetail.setTitle("Url Expired");
        problemDetail.setType(URI.create(ERRORS_BASE_URL + "/url-expired"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = UrlNotFoundException.class)
    public ProblemDetail handleUrlNotFound(UrlNotFoundException exception) {
        log.debug("URL not found: {}", exception.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("URL Not Found");
        problemDetail.setType(URI.create(ERRORS_BASE_URL + "/url-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("Invalid argument: {}", exception.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());

        problemDetail.setType(URI.create(ERRORS_BASE_URL + "/invalid-argument"));
        problemDetail.setTitle("Invalid Argument");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public ProblemDetail handleGenericException(Exception exception) {
        log.error("Unexpected error occurred: {}", exception.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create(ERRORS_BASE_URL + "/internal-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        if (log.isDebugEnabled()) {
            problemDetail.setProperty("debugMessage", exception.getMessage());
        }

        return problemDetail;
    }
}
