package com.shorty.urls;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shorty.common.exception.ValidationException;
import com.shorty.common.util.UrlUtils;
import com.shorty.users.User;
import com.shorty.users.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

  @Mock private UrlUtils urlUtils;
  @Mock private UrlRepository urlRepository;
  @Mock private UserRepository userRepository;
  @InjectMocks private UrlService urlService;

  private User user;

  @BeforeEach
  void setUp() {

    user =
        User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .password("hashedPassword")
            .build();
  }

  @Test
  void createShortUrl_ValidUrl_ReturnsUrl() {
    // Given
    String originalUrl = "https://example.com";
    String customCode = "abc123";
    Url url = Url.builder().originalUrl(originalUrl).shortCode(customCode).build();

    when(urlUtils.isValidUrl(anyString())).thenReturn(true);
    when(urlUtils.normalizeUrl(anyString())).thenReturn(originalUrl);
    when(urlUtils.generateShortCode()).thenReturn(customCode);
    when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
    when(urlRepository.save(any(Url.class))).thenReturn(url);

    // When
    Url result =
        urlService.createShortUrl(
            originalUrl, null, UrlVisibility.PUBLIC, LocalDateTime.now().plusDays(7), -1, user);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getOriginalUrl()).isEqualTo(originalUrl);
    assertThat(result.getShortCode()).isEqualTo(customCode);
    assertThat(result.isActive()).isTrue();
  }

  @Test
  void createShortUrl_WithExpiration_SetsExpirationDate() {
    // Given
    String originalUrl = "https://example.com";
    String customCode = "custom123";
    LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
    Url url = Url.builder().originalUrl(originalUrl).expiresAt(expiresAt).build();

    when(urlUtils.isValidUrl(anyString())).thenReturn(true);
    when(urlUtils.normalizeUrl(anyString())).thenReturn(originalUrl);
    when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
    when(urlRepository.save(any(Url.class))).thenReturn(url);

    // When
    Url result =
        urlService.createShortUrl(
            originalUrl, customCode, UrlVisibility.PUBLIC, expiresAt, -1, user);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
  }

  @Test
  void createShortUrl_WithClickLimit_SetsClickLimit() {
    // Given
    String originalUrl = "https://example.com";
    Integer clickLimit = 100;

    when(urlUtils.isValidUrl(anyString())).thenReturn(true);
    when(urlUtils.normalizeUrl(anyString())).thenReturn(originalUrl);
    when(urlUtils.generateShortCode()).thenReturn("abc123");
    when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
    when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Url result = urlService.createShortUrl(originalUrl, null, null, null, clickLimit, null);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getClickLimit()).isEqualTo(clickLimit);
    verify(urlRepository).save(any(Url.class));
  }

  @Test
  void createShortUrl_InvalidUrl_ThrowsValidationException() {
    // Given
    String invalidUrl = "not-a-url";
    when(urlUtils.isValidUrl(anyString())).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> urlService.createShortUrl(invalidUrl, null, null, null, null, user))
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
        () -> urlService.createShortUrl("https://another.com", customCode, null, null, null, user));
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

  @Test
  void resetClickCount_ValidUrl_ResetsCount() {
    // Given
    UUID urlId = UUID.randomUUID();
    UUID userId = user.getId();

    Url url = new Url("https://example.com", "abc123", user);
    url.setClickCount(50);

    when(urlRepository.findById(any(UUID.class))).thenReturn(Optional.of(url));
    when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

    // When
    Url result = urlService.resetClickCount(urlId, userId);

    // Then
    assertThat(result.getClickCount()).isEqualTo(0);
    verify(urlRepository).save(url);
  }

  @Test
  void cleanupExpiredUrls_ExpiredUrls_DeactivatesThem() {
    // Given
    LocalDateTime now = LocalDateTime.now();

    Url expiredUrl1 = new Url("https://example1.com", "abc123", user);
    expiredUrl1.setExpiresAt(now.minusDays(1));
    expiredUrl1.setActive(true);

    Url expiredUrl2 = new Url("https://example2.com", "def456", user);
    expiredUrl2.setExpiresAt(now.minusHours(1));
    expiredUrl2.setActive(true);

    when(urlRepository.findExpiredUrls(any(LocalDateTime.class)))
        .thenReturn(List.of(expiredUrl1, expiredUrl2));
    when(urlRepository.saveAll(anyList())).thenReturn(List.of(expiredUrl1, expiredUrl2));

    // When
    int count = urlService.cleanupExpiredUrls();

    // Then
    assertThat(count).isEqualTo(2);
    assertThat(expiredUrl1.isActive()).isFalse();
    assertThat(expiredUrl2.isActive()).isFalse();
    verify(urlRepository).saveAll(anyList());
  }
}
