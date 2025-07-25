package com.shorty.urls;

import com.shorty.urls.dto.PasswordVerificationRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class RedirectionController {

  private final UrlService urlService;

  public RedirectionController(UrlService urlService) {
    this.urlService = urlService;
  }

  @GetMapping("/{shortCode}")
  public void redirect(
      @PathVariable String shortCode,
      @RequestParam(required = false) String password,
      HttpServletRequest request,
      HttpServletResponse response)
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

    if (url.isPasswordProtected()) {
      if (!urlService.verifyUrlPassword(shortCode, password)) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Password required\",\"passwordRequired\":true}");
        return;
      }
    }

    urlService.logClickCount(url.getId());
    response.sendRedirect(url.getOriginalUrl());
  }

  @PostMapping("/api/v1/verify-password/{shortCode}")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> verifyPassword(
      @PathVariable String shortCode, @RequestBody PasswordVerificationRequest request) {
    boolean isValid = urlService.verifyUrlPassword(shortCode, request.password());

    if (isValid) {
      String accessToken = generateAccessToken(shortCode);

      return ResponseEntity.ok(
          Map.of(
              "valid",
              true,
              "accessToken",
              accessToken,
              "redirectUrl",
              "/" + shortCode + "?token=" + accessToken));
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("valid", false, "error", "Invalid password"));
    }
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
            "passwordProtected", url.isPasswordProtected(),
            "clickCount", url.getClickCount(),
            "remainingClicks", url.getRemainingClicks(),
            "description", url.getDescription() != null ? url.getDescription() : "",
            "createdAt", url.getCreatedAt());

    return ResponseEntity.ok(info);
  }

  private String generateAccessToken(String shortCode) {
    return shortCode + "_" + System.currentTimeMillis();
  }
}
