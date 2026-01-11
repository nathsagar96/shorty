package com.shorty.dtos.responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record UrlStatsResponse(
        UUID id,
        String originalUrl,
        String shortCode,
        String shortUrl,
        Long clicks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
