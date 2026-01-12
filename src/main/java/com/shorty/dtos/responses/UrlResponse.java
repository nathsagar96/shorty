package com.shorty.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record UrlResponse(
        @Schema(description = "Unique identifier for the URL mapping", example = "550e8400-e29b-41d4-a716-446655440000")
                UUID id,
        @Schema(description = "The generated short code for the URL", example = "abc123") String shortCode,
        @Schema(description = "The full short URL", example = "https://shorty.com/abc123") String shortUrl,
        @Schema(description = "The original long URL", example = "https://www.example.com") String originalUrl,
        @Schema(description = "The number of clicks this short URL has received", example = "15") Long clickCount,
        @Schema(description = "Expiration timestamp", example = "2023-12-31T23:59:59Z") Instant expiresAt,
        @Schema(description = "Creation timestamp", example = "2023-01-01T00:00:00Z") Instant createdAt) {}
