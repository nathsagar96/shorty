package com.shorty.urls;

import java.time.LocalDateTime;

public record Url(
    String id, String originalUrl, String shortCode, LocalDateTime createdAt, boolean isActive) {

  public Url {
    if (originalUrl == null || originalUrl.trim().isEmpty()) {
      throw new IllegalArgumentException("Original URL cannot be null or empty");
    }
    if (shortCode == null || shortCode.trim().isEmpty()) {
      throw new IllegalArgumentException("Short code cannot be null or empty");
    }
  }

  public static Url create(String originalUrl, String shortCode) {
    return new Url(
        java.util.UUID.randomUUID().toString(),
        originalUrl.trim(),
        shortCode,
        LocalDateTime.now(),
        true);
  }

  public Url withActiveStatus(boolean active) {
    return new Url(id, originalUrl, shortCode, createdAt, active);
  }
}
