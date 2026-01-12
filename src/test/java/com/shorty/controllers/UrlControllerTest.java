package com.shorty.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shorty.dtos.requests.CreateUrlRequest;
import com.shorty.dtos.responses.UrlResponse;
import com.shorty.exceptions.UrlNotFoundException;
import com.shorty.services.UrlService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@WebMvcTest(controllers = UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Create Short URL Tests")
    class CreateShortUrlTests {

        @Test
        @DisplayName("Should return 201 when creating short URL with valid request")
        void shouldReturn201WhenCreatingShortUrl() throws Exception {
            // Given
            CreateUrlRequest request = new CreateUrlRequest("https://example.com", null, null);
            UrlResponse expectedResponse = new UrlResponse(
                    UUID.randomUUID(),
                    "abc123",
                    "http://localhost:8080/abc123",
                    "https://example.com",
                    0L,
                    Instant.now().plusSeconds(604800),
                    Instant.now());

            when(urlService.createShortUrl(any(CreateUrlRequest.class))).thenReturn(expectedResponse);

            // When/Then
            mockMvc.perform(post("/api/v1/urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.shortUrl").value(expectedResponse.shortUrl()))
                    .andExpect(jsonPath("$.originalUrl").value(expectedResponse.originalUrl()));

            verify(urlService, times(1)).createShortUrl(any(CreateUrlRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request is invalid")
        void shouldReturn400WhenRequestIsInvalid() throws Exception {
            // Given
            CreateUrlRequest invalidRequest = new CreateUrlRequest("", null, null);

            // When/Then
            mockMvc.perform(post("/api/v1/urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(urlService, never()).createShortUrl(any(CreateUrlRequest.class));
        }
    }

    @Nested
    @DisplayName("Get URL Details Tests")
    class GetUrlDetailsTests {

        @Test
        @DisplayName("Should return 200 when getting URL details for existing short code")
        void shouldReturn200WhenGettingUrlDetails() throws Exception {
            // Given
            String shortCode = "abc123";
            UrlResponse expectedResponse = new UrlResponse(
                    UUID.randomUUID(),
                    shortCode,
                    "http://localhost:8080/" + shortCode,
                    "https://example.com",
                    5L,
                    Instant.now().plusSeconds(604800),
                    Instant.now());

            when(urlService.getUrlDetails(shortCode)).thenReturn(expectedResponse);

            // When/Then
            mockMvc.perform(get("/api/v1/urls/{shortCode}", shortCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.shortCode").value(shortCode))
                    .andExpect(jsonPath("$.clickCount").value(5));

            verify(urlService, times(1)).getUrlDetails(shortCode);
        }

        @Test
        @DisplayName("Should return 404 when short code not found")
        void shouldReturn404WhenShortCodeNotFound() throws Exception {
            // Given
            String nonExistentCode = "nonexist";

            when(urlService.getUrlDetails(nonExistentCode)).thenThrow(new UrlNotFoundException("URL not found"));

            // When/Then
            mockMvc.perform(get("/api/v1/urls/{shortCode}", nonExistentCode)).andExpect(status().isNotFound());

            verify(urlService, times(1)).getUrlDetails(nonExistentCode);
        }
    }

    @Nested
    @DisplayName("Delete Short URL Tests")
    class DeleteShortUrlTests {

        @Test
        @DisplayName("Should return 204 when deleting existing short URL")
        void shouldReturn204WhenDeletingShortUrl() throws Exception {
            // Given
            String shortCode = "abc123";

            doNothing().when(urlService).deleteShortUrl(shortCode);

            // When/Then
            mockMvc.perform(delete("/api/v1/urls/{shortCode}", shortCode)).andExpect(status().isNoContent());

            verify(urlService, times(1)).deleteShortUrl(shortCode);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent short URL")
        void shouldReturn404WhenDeletingNonExistentShortUrl() throws Exception {
            // Given
            String nonExistentCode = "nonexist";

            doThrow(new UrlNotFoundException("URL not found")).when(urlService).deleteShortUrl(nonExistentCode);

            // When/Then
            mockMvc.perform(delete("/api/v1/urls/{shortCode}", nonExistentCode)).andExpect(status().isNotFound());

            verify(urlService, times(1)).deleteShortUrl(nonExistentCode);
        }
    }
}
