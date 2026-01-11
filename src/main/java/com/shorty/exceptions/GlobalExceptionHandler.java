package com.shorty.exceptions;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = UrlNotFoundException.class)
    ProblemDetail handleUrlNotFoundException(UrlNotFoundException exception) {
        log.error("UrlNotFoundException: {}", exception.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Not Found");
        problemDetail.setType(URI.create("https://shorty.com/errors/not-found"));

        return problemDetail;
    }

    @ExceptionHandler(value = Exception.class)
    ProblemDetail handleException(Exception exception) {
        log.error("Exception: {}", exception.getMessage());

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://shorty.com/errors/internal-server-error"));

        return problemDetail;
    }
}
