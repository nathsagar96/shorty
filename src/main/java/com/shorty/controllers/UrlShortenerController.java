package com.shorty.controllers;

import com.shorty.dtos.requests.CreateUrlRequest;
import com.shorty.dtos.responses.UrlResponse;
import com.shorty.services.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/urls")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping
    ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody CreateUrlRequest request) {
        UrlResponse response = urlShortenerService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
