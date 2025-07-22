package com.shorty.urls;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shorty.urls.dto.CreateUrlRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UrlControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void createShortUrl_ValidRequest_ReturnsCreated() throws Exception {
    // Given
    CreateUrlRequest request = new CreateUrlRequest("https://example.com", null);

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
        .andExpect(jsonPath("$.shortCode").isNotEmpty())
        .andExpect(jsonPath("$.shortUrl").isNotEmpty())
        .andExpect(jsonPath("$.isActive").value(true));
  }

  @Test
  void createShortUrl_InvalidUrl_ReturnsBadRequest() throws Exception {
    // Given
    CreateUrlRequest request = new CreateUrlRequest("invalid-url", null);

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void redirect_ExistingShortCode_RedirectsToOriginalUrl() throws Exception {
    // Given - create a short URL first
    CreateUrlRequest request = new CreateUrlRequest("https://example.com", "test123");

    mockMvc
        .perform(
            post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // When & Then
    mockMvc
        .perform(get("/test123"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", "https://example.com"));
  }

  @Test
  void redirect_NonExistingShortCode_ReturnsNotFound() throws Exception {
    // When & Then
    mockMvc.perform(get("/nonexistent")).andExpect(status().isNotFound());
  }
}
