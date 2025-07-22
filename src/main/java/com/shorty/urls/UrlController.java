package com.shorty.urls;

import com.shorty.urls.dto.CreateUrlRequest;
import com.shorty.urls.dto.UrlResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UrlController {

  private final UrlService urlService;
  private final String baseUrl;

  public UrlController(
      UrlService urlService, @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
    this.urlService = urlService;
    this.baseUrl = baseUrl;
  }

  @PostMapping("/urls")
  public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody CreateUrlRequest request) {
    Url url = urlService.createShortUrl(request.url(), request.customCode());
    UrlResponse response = UrlResponse.from(url, baseUrl);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/urls")
  public ResponseEntity<List<UrlResponse>> getAllUrls() {
    List<UrlResponse> responses =
        urlService.getAllUrls().stream().map(url -> UrlResponse.from(url, baseUrl)).toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/urls/count")
  public ResponseEntity<Long> getUrlCount() {
    return ResponseEntity.ok(urlService.getUrlCount());
  }
}
