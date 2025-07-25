package com.shorty.urls.dto;

import java.util.List;
import java.util.UUID;

public record BulkToggleStatusRequest(List<UUID> urlIds, boolean active) {}
