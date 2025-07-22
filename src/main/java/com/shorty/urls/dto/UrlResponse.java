package com.shorty.urls.dto;

import com.shorty.urls.Url;
import java.time.LocalDateTime;

public record UrlResponse(
    String id,
    String originalUrl,
    String shortCode,
    String shortUrl,
    LocalDateTime createdAt,
    boolean isActive) {

  public static UrlResponse from(Url url, String baseUrl) {
    return new UrlResponse(
        url.id(),
        url.originalUrl(),
        url.shortCode(),
        baseUrl + "/" + url.shortCode(),
        url.createdAt(),
        url.isActive());
  }
}
