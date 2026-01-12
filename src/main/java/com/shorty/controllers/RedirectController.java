package com.shorty.controllers;

import com.shorty.dtos.responses.RedirectResponse;
import com.shorty.services.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Redirect", description = "Redirect operations for short URLs")
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @Operation(
            summary = "Redirect to original URL",
            description = "Redirects to the original URL associated with the given short code")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "302", description = "Successfully redirected to original URL"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Short code not found",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(responseCode = "410", description = "Short code expired")
            })
    @GetMapping(value = "/{shortCode}", produces = MediaType.TEXT_PLAIN_VALUE)
    public void redirectToOriginalUrl(
            @Parameter(description = "The short code to redirect", required = true) @PathVariable String shortCode,
            HttpServletResponse response) {
        log.debug("Redirecting short code: {}", shortCode);

        RedirectResponse redirectData = urlService.resolveAndTrack(shortCode);

        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", redirectData.originalUrl());
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    }
}
