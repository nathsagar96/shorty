package com.shorty.urls.dto;

import com.shorty.urls.Url;
import com.shorty.urls.UrlVisibility;
import java.time.LocalDateTime;
import java.util.UUID;

public record UrlResponse(
    UUID id,
    String originalUrl,
    String shortCode,
    String shortUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean active,
    UrlVisibility visibility,
    String ownerEmail,
    LocalDateTime expiresAt,
    boolean expired,
    int clickLimit,
    int clickCount,
    int remainingClicks,
    UrlStats stats) {
  public static UrlResponse from(Url url, String baseUrl) {
    return new UrlResponse(
        url.getId(),
        url.getOriginalUrl(),
        url.getShortCode(),
        baseUrl + "/" + url.getShortCode(),
        url.getCreatedAt(),
        url.getUpdatedAt(),
        url.isActive(),
        url.getVisibility(),
        url.getUser() != null ? url.getUser().getEmail() : null,
        url.getExpiresAt(),
        url.isExpired(),
        url.getClickLimit(),
        url.getClickCount(),
        url.getRemainingClicks(),
        new UrlStats(url.isAccessible(), url.isClickLimitReached()));
  }
}
