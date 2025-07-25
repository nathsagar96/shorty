package com.shorty.urls;

import com.shorty.security.CustomUserDetails;
import com.shorty.urls.dto.CreateUrlRequest;
import com.shorty.urls.dto.UpdateUrlRequest;
import com.shorty.urls.dto.UrlResponse;
import jakarta.validation.Valid;
import java.util.List;
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
  private final String baseUrl;

  public UrlController(
      UrlService urlService, @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
    this.urlService = urlService;
    this.baseUrl = baseUrl;
  }

  @PostMapping
  public ResponseEntity<UrlResponse> createShortUrl(
      @Valid @RequestBody CreateUrlRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Url url;
    if (userDetails != null) {
      url =
          urlService.createShortUrl(
              request.url(), request.customCode(), request.visibility(), userDetails.user());
    } else {
      url = urlService.createShortUrl(request.url(), request.customCode());
    }

    UrlResponse response = UrlResponse.from(url, baseUrl);
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
      @RequestBody UpdateUrlRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Url url =
        urlService.updateUrl(
            urlId, userDetails.user().getId(), request.originalUrl(), request.visibility());

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
}
