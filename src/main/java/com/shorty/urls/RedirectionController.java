package com.shorty.urls;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class RedirectionController {

  private final UrlService urlService;

  @GetMapping("/{shortCode}")
  public void redirect(@PathVariable String shortCode, HttpServletResponse response)
      throws IOException {

    Optional<Url> urlOpt = urlService.findByShortCode(shortCode);

    if (urlOpt.isEmpty()) {
      response.sendError(HttpStatus.NOT_FOUND.value(), "URL not found");
      return;
    }

    Url url = urlOpt.get();

    if (!url.isAccessible()) {
      if (url.isExpired()) {
        response.sendError(HttpStatus.GONE.value(), "URL has expired");
        return;
      }

      if (url.isClickLimitReached()) {
        response.sendError(HttpStatus.GONE.value(), "URL click limit reached");
        return;
      }

      if (!url.isActive()) {
        response.sendError(HttpStatus.GONE.value(), "URL is inactive");
        return;
      }
    }

    urlService.logClickCount(url.getId());
    response.sendRedirect(url.getOriginalUrl());
  }

  @GetMapping("/api/v1/info/{shortCode}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getUrlInfo(@PathVariable String shortCode) {
    Optional<Url> urlOpt = urlService.findByShortCode(shortCode);

    if (urlOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Url url = urlOpt.get();

    Map<String, Object> info =
        Map.of(
            "shortCode", url.getShortCode(),
            "active", url.isActive(),
            "expired", url.isExpired(),
            "clickCount", url.getClickCount(),
            "remainingClicks", url.getRemainingClicks(),
            "createdAt", url.getCreatedAt());

    return ResponseEntity.ok(info);
  }
}
