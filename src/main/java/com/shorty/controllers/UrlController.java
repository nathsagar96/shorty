package com.shorty.controllers;

import com.shorty.dtos.requests.CreateUrlRequest;
import com.shorty.dtos.responses.PageResponse;
import com.shorty.dtos.responses.UrlResponse;
import com.shorty.services.UrlService;
import com.shorty.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/urls")
@Tag(name = "URL Management", description = "Operations for creating, retrieving, and managing short URLs")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Get all URLs", description = "Retrieve a paginated list of all URLs")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved list of URLs",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = PageResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request parameters",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetail.class)))
            })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageResponse<UrlResponse>> getAllUrls(
            @Parameter(description = "Page number to retrieve") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") Integer size) {
        UUID userId = securityUtils.getCurrentUserId();
        var response = urlService.getAllUrls(page, size, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Create a new short URL",
            description = "Creates a new short URL mapping for the given original URL")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Short URL created successfully",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = UrlResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request data",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "Custom alias already exists",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetail.class))),
            })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody CreateUrlRequest request) {
        log.info("Received request to create short URL");
        UUID userId = securityUtils.getCurrentUserId();
        UrlResponse response = urlService.createShortUrl(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get URL details",
            description = "Retrieves details about a short URL including click count and expiration")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved URL details",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = UrlResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Short code not found",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetail.class))),
            })
    @GetMapping(value = "/{shortCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UrlResponse> getUrlDetails(
            @Parameter(description = "The short code to retrieve details for", required = true) @PathVariable
                    String shortCode) {
        log.debug("Retrieving details for short code: {}", shortCode);
        UUID userId = securityUtils.getCurrentUserId();
        UrlResponse response = urlService.getUrlDetails(shortCode, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a short URL", description = "Deletes a short URL mapping by its short code")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Successfully deleted the short URL"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Short code not found",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetail.class))),
            })
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(
            @Parameter(description = "The short code to delete", required = true) @PathVariable String shortCode) {
        log.info("Deleting short code: {}", shortCode);
        UUID userId = securityUtils.getCurrentUserId();
        urlService.deleteShortUrl(shortCode, userId);
        return ResponseEntity.noContent().build();
    }
}
