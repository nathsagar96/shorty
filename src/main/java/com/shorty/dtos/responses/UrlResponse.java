package com.shorty.dtos.responses;

import java.time.Instant;
import java.util.UUID;

public record UrlResponse(
        UUID id,
        String shortCode,
        String shortUrl,
        String originalUrl,
        Long clickCount,
        Instant expiresAt,
        Instant createdAt) {}
