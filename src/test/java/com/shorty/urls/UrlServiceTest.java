package com.shorty.urls;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.shorty.common.exception.ValidationException;
import com.shorty.common.util.UrlUtils;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

  @Mock private UrlUtils urlUtils;
  @Mock private UrlRepository urlRepository;

  @InjectMocks private UrlService urlService;

  @Test
  void createShortUrl_ValidUrl_ReturnsUrl() {
    // Given
    String originalUrl = "https://example.com";
    String shortCode = "abc123";
    Url url = new Url(originalUrl, shortCode);

    when(urlUtils.isValidUrl(anyString())).thenReturn(true);
    when(urlUtils.normalizeUrl(anyString())).thenReturn(originalUrl);
    when(urlUtils.generateShortCode()).thenReturn(shortCode);
    when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
    when(urlRepository.save(any())).thenReturn(url);

    // When
    Url result = urlService.createShortUrl(originalUrl, null);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getOriginalUrl()).isEqualTo(originalUrl);
    assertThat(result.getShortCode()).isEqualTo(shortCode);
    assertThat(result.isActive()).isTrue();
  }

  @Test
  void createShortUrl_InvalidUrl_ThrowsValidationException() {
    // Given
    String invalidUrl = "not-a-url";
    when(urlUtils.isValidUrl(anyString())).thenReturn(false);

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
    when(urlUtils.normalizeUrl(anyString())).thenReturn(originalUrl);
    when(urlRepository.existsByShortCode(anyString())).thenReturn(true);

    // When & Then - try to create another with same custom code
    assertThrows(
        ValidationException.class,
        () -> urlService.createShortUrl("https://another.com", customCode));
  }

  @Test
  void findByShortCode_ExistingCode_ReturnsUrl() {
    // Given
    String shortCode = "abc123";
    Url created = new Url("https://example.com", shortCode);

    when(urlRepository.findByShortCodeAndActiveTrue(anyString())).thenReturn(Optional.of(created));

    // When
    Optional<Url> result = urlService.findByShortCode(shortCode);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(created.getId());
  }

  @Test
  void findByShortCode_NonExistingCode_ReturnsEmpty() {
    // When
    Optional<Url> result = urlService.findByShortCode("nonexistent");

    // Then
    assertThat(result).isEmpty();
  }
}
