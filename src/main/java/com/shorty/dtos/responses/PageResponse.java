package com.shorty.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PageResponse<T>(
        @Schema(description = "List of elements") List<T> content,
        @Schema(description = "Current page number (0-based)", example = "0") int pageNumber,
        @Schema(description = "Number of items per page", example = "10") int pageSize,
        @Schema(description = "Total number of items", example = "100") long totalElements,
        @Schema(description = "Total number of pages", example = "10") int totalPages,
        @Schema(description = "Whether this is the first page", example = "true") boolean first,
        @Schema(description = "Whether this is the last page", example = "false") boolean last) {}
