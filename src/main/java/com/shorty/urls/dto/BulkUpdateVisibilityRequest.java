package com.shorty.urls.dto;

import com.shorty.urls.UrlVisibility;
import java.util.List;
import java.util.UUID;

public record BulkUpdateVisibilityRequest(List<UUID> urlIds, UrlVisibility visibility) {}
