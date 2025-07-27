package com.shorty.urls.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record BulkDeleteRequest(
    @NotEmpty(message = "URL ID list cannot be empty") List<UUID> urlIds) {}
