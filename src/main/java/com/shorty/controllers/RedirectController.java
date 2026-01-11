package com.shorty.controllers;

import com.shorty.services.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class RedirectController {

    private final UrlShortenerService urlShortenerService;

    @GetMapping("/{shortCode}")
    String redirect(@PathVariable String shortCode) {
        String originalUrl = urlShortenerService.getOriginalUrlAndTrackClick(shortCode);
        return "redirect:" + originalUrl;
    }
}
