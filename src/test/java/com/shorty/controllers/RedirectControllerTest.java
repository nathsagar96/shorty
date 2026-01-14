package com.shorty.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shorty.dtos.responses.RedirectResponse;
import com.shorty.exceptions.UrlExpiredException;
import com.shorty.exceptions.UrlNotFoundException;
import com.shorty.services.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = RedirectController.class)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Nested
    @DisplayName("Redirect Tests")
    class RedirectTests {

        @Test
        @DisplayName("Should redirect to original URL when short code is valid")
        void shouldRedirectToOriginalUrlWhenValid() throws Exception {
            // Given
            String shortCode = "abc123";
            String originalUrl = "https://example.com";
            RedirectResponse redirectResponse = new RedirectResponse(originalUrl, 1L);

            when(urlService.resolveAndTrack(shortCode)).thenReturn(redirectResponse);

            // When/Then
            mockMvc.perform(get("/{shortCode}", shortCode))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", originalUrl))
                    .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"));

            verify(urlService, times(1)).resolveAndTrack(shortCode);
        }

        @Test
        @DisplayName("Should return 404 when short code not found")
        void shouldReturn404WhenShortCodeNotFound() throws Exception {
            // Given
            String nonExistentCode = "nonexist";

            when(urlService.resolveAndTrack(nonExistentCode)).thenThrow(new UrlNotFoundException("URL not found"));

            // When/Then
            mockMvc.perform(get("/{shortCode}", nonExistentCode)).andExpect(status().isNotFound());

            verify(urlService, times(1)).resolveAndTrack(nonExistentCode);
        }

        @Test
        @DisplayName("Should return 410 when short code expired")
        void shouldReturn410WhenShortCodeExpired() throws Exception {
            // Given
            String expiredCode = "expired";

            when(urlService.resolveAndTrack(expiredCode)).thenThrow(new UrlExpiredException("URL expired"));

            // When/Then
            mockMvc.perform(get("/{shortCode}", expiredCode)).andExpect(status().isGone());

            verify(urlService, times(1)).resolveAndTrack(expiredCode);
        }
    }
}
