package com.shorty.urls.dto;

import java.util.List;

public record BulkOperationResponse<T>(
    List<T> successful,
    List<BulkError> errors,
    int totalProcessed,
    int successCount,
    int errorCount) {}
