package com.shorty.controllers;

import com.shorty.dtos.responses.RedirectResponse;
import com.shorty.services.UrlService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortCode}")
    public void redirectToOriginalUrl(@PathVariable String shortCode, HttpServletResponse response) {
        log.debug("Redirecting short code: {}", shortCode);

        RedirectResponse redirectData = urlService.resolveAndTrack(shortCode);

        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", redirectData.originalUrl());
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    }
}
