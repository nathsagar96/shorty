package com.shorty.urls;

import com.shorty.security.CustomUserDetails;
import com.shorty.urls.dto.*;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls/bulk")
public class BulkUrlController {
  private final BulkUrlService bulkUrlService;
  private final String baseUrl;

  public BulkUrlController(
      BulkUrlService bulkUrlService,
      @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
    this.bulkUrlService = bulkUrlService;
    this.baseUrl = baseUrl;
  }

  @PostMapping()
  public ResponseEntity<BulkOperationResponse<UrlResponse>> bulkCreateUrls(
      @Valid @RequestBody BulkCreateUrlRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    BulkOperationResponse<UrlResponse> response =
        bulkUrlService.bulkCreateUrls(request, userDetails.user(), baseUrl);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  public ResponseEntity<BulkOperationResponse<UUID>> bulkDeleteUrls(
      @RequestBody BulkDeleteRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    BulkOperationResponse<UUID> response =
        bulkUrlService.bulkDeleteUrls(request.urlIds(), userDetails.user().getId());

    return ResponseEntity.ok(response);
  }

  @PutMapping("/visibility")
  public ResponseEntity<BulkOperationResponse<UrlResponse>> bulkUpdateVisibility(
      @RequestBody BulkUpdateVisibilityRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    BulkOperationResponse<UrlResponse> response =
        bulkUrlService.bulkUpdateVisibility(
            request.urlIds(), request.visibility(), userDetails.user().getId(), baseUrl);

    return ResponseEntity.ok(response);
  }

  @PutMapping("/status")
  public ResponseEntity<BulkOperationResponse<UrlResponse>> bulkToggleStatus(
      @RequestBody BulkToggleStatusRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    BulkOperationResponse<UrlResponse> response =
        bulkUrlService.bulkToggleStatus(
            request.urlIds(), request.active(), userDetails.user().getId(), baseUrl);

    return ResponseEntity.ok(response);
  }
}
