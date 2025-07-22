package com.shorty.urls;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.shorty.common.util.UrlUtils;
import com.shorty.common.util.exception.ValidationException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

  @Mock private UrlUtils urlUtils;

  @InjectMocks private UrlService urlService;

  @Test
  void createShortUrl_ValidUrl_ReturnsUrl() {
    // Given
    String originalUrl = "https://example.com";
    String shortCode = "abc123";

    when(urlUtils.isValidUrl(originalUrl)).thenReturn(true);
    when(urlUtils.normalizeUrl(originalUrl)).thenReturn(originalUrl);
    when(urlUtils.generateShortCode()).thenReturn(shortCode);

    // When
    Url result = urlService.createShortUrl(originalUrl, null);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.originalUrl()).isEqualTo(originalUrl);
    assertThat(result.shortCode()).isEqualTo(shortCode);
    assertThat(result.isActive()).isTrue();
  }

  @Test
  void createShortUrl_InvalidUrl_ThrowsValidationException() {
    // Given
    String invalidUrl = "not-a-url";
    when(urlUtils.isValidUrl(invalidUrl)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> urlService.createShortUrl(invalidUrl, null))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Invalid URL format");
  }

  @Test
  void createShortUrl_CustomCodeExists_ThrowsValidationException() {
    // Given
    String originalUrl = "https://example.com";
    String customCode = "custom";

    when(urlUtils.isValidUrl(anyString())).thenReturn(true);
    when(urlUtils.normalizeUrl(originalUrl)).thenReturn(originalUrl);

    // Create first URL with custom code
    urlService.createShortUrl(originalUrl, customCode);

    // When & Then - try to create another with same custom code
    assertThatThrownBy(() -> urlService.createShortUrl("https://another.com", customCode))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Short code already exists: " + customCode);
  }

  @Test
  void findByShortCode_ExistingCode_ReturnsUrl() {
    // Given
    String originalUrl = "https://example.com";
    String shortCode = "abc123";

    when(urlUtils.isValidUrl(originalUrl)).thenReturn(true);
    when(urlUtils.normalizeUrl(originalUrl)).thenReturn(originalUrl);
    when(urlUtils.generateShortCode()).thenReturn(shortCode);

    Url created = urlService.createShortUrl(originalUrl, null);

    // When
    Optional<Url> result = urlService.findByShortCode(shortCode);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().id()).isEqualTo(created.id());
  }

  @Test
  void findByShortCode_NonExistingCode_ReturnsEmpty() {
    // When
    Optional<Url> result = urlService.findByShortCode("nonexistent");

    // Then
    assertThat(result).isEmpty();
  }
}
