package com.shorty.controllers;

import com.shorty.dtos.requests.CreateUrlRequest;
import com.shorty.dtos.responses.UrlResponse;
import com.shorty.services.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody CreateUrlRequest request) {
        log.info("Received request to create short URL");
        UrlResponse response = urlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/{shortCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UrlResponse> getUrlDetails(@PathVariable String shortCode) {
        log.debug("Retrieving details for short code: {}", shortCode);
        UrlResponse response = urlService.getUrlDetails(shortCode);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String shortCode) {
        log.info("Deleting short code: {}", shortCode);
        urlService.deleteShortUrl(shortCode);
        return ResponseEntity.noContent().build();
    }
}
