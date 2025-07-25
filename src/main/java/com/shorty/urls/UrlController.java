package com.shorty.urls;

import com.shorty.common.validation.UrlValidationService;
import com.shorty.security.CustomUserDetails;
import com.shorty.urls.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

  private final UrlService urlService;
  private final UrlValidationService validationService;
  private final String baseUrl;

  public UrlController(
      UrlService urlService,
      UrlValidationService validationService,
      @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
    this.urlService = urlService;
    this.validationService = validationService;
    this.baseUrl = baseUrl;
  }

  @PostMapping
  public ResponseEntity<?> createShortUrl(
      @Valid @RequestBody CreateUrlRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    var urlValidation = validationService.validateUrl(request.url());
    if (!urlValidation.isValid()) {
      return ResponseEntity.badRequest()
          .body(
              Map.of(
                  "errors", urlValidation.getErrors(),
                  "warnings", urlValidation.getWarnings()));
    }

    var codeValidation = validationService.validateCustomCode(request.customCode());
    if (!codeValidation.isValid()) {
      return ResponseEntity.badRequest().body(Map.of("errors", codeValidation.getErrors()));
    }

    var expirationValidation = validationService.validateExpirationDate(request.expiresAt());
    if (!expirationValidation.isValid()) {
      return ResponseEntity.badRequest().body(Map.of("errors", expirationValidation.getErrors()));
    }

    Url url =
        urlService.createShortUrl(
            request.url(),
            request.customCode(),
            request.visibility(),
            request.expiresAt(),
            request.clickLimit(),
            request.description(),
            request.password(),
            userDetails.user());

    UrlResponse response = UrlResponse.from(url, baseUrl);

    if (!urlValidation.getWarnings().isEmpty()) {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(Map.of("url", response, "warnings", urlValidation.getWarnings()));
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<UrlResponse>> getUserUrls(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<Url> urls = urlService.getUserUrls(userDetails.user().getId());
    List<UrlResponse> responses = urls.stream().map(url -> UrlResponse.from(url, baseUrl)).toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/public")
  public ResponseEntity<Page<UrlResponse>> getPublicUrls(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Url> urls = urlService.getPublicUrls(pageable);
    Page<UrlResponse> responses = urls.map(url -> UrlResponse.from(url, baseUrl));
    return ResponseEntity.ok(responses);
  }

  @PutMapping("/{urlId}")
  public ResponseEntity<UrlResponse> updateUrl(
      @PathVariable UUID urlId,
      @Valid @RequestBody UpdateUrlRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Url url =
        urlService.updateUrl(
            urlId,
            userDetails.user().getId(),
            request.originalUrl(),
            request.visibility(),
            request.expiresAt(),
            request.clickLimit(),
            request.description(),
            request.password(),
            request.removePassword());

    UrlResponse response = UrlResponse.from(url, baseUrl);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{urlId}/toggle")
  public ResponseEntity<UrlResponse> toggleUrlStatus(
      @PathVariable UUID urlId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Url url = urlService.toggleUrlStatus(urlId, userDetails.user().getId());
    UrlResponse response = UrlResponse.from(url, baseUrl);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{urlId}")
  public ResponseEntity<Void> deleteUrl(
      @PathVariable UUID urlId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    urlService.deleteUrl(urlId, userDetails.user().getId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/count")
  public ResponseEntity<Long> getUrlCount() {
    return ResponseEntity.ok(urlService.getUrlCount());
  }

  @GetMapping("/expiring")
  public ResponseEntity<List<UrlResponse>> getUrlsExpiringSoon(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "24") int hours) {
    List<Url> urls = urlService.getUrlsExpiringSoon(userDetails.user().getId(), hours);

    List<UrlResponse> response = urls.stream().map(url -> UrlResponse.from(url, baseUrl)).toList();

    return ResponseEntity.ok(response);
  }

  @PostMapping("/{urlId}/reset-clicks")
  public ResponseEntity<UrlResponse> resetClickCount(
      @PathVariable UUID urlId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Url url = urlService.resetClickCount(urlId, userDetails.user().getId());
    UrlResponse response = UrlResponse.from(url, baseUrl);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{urlId}/extend")
  public ResponseEntity<UrlResponse> extendExpiration(
      @PathVariable UUID urlId,
      @RequestBody ExtendExpirationRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Url url =
        urlService.extendExpiration(urlId, userDetails.user().getId(), request.newExpirationDate());

    UrlResponse response = UrlResponse.from(url, baseUrl);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/validate")
  public ResponseEntity<Map<String, Object>> validateUrl(@RequestBody ValidateUrlRequest request) {
    var validation = validationService.validateUrl(request.url());

    return ResponseEntity.ok(
        Map.of(
            "valid", validation.isValid(),
            "errors", validation.getErrors(),
            "warnings", validation.getWarnings(),
            "info", validation.getInfo()));
  }
}
