package com.shorty.urls;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

  @Mock private UrlUtils urlUtils;
  @Mock private UrlRepository urlRepository;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private UrlService urlService;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "Test", "User", "hashedPassword");
    try {
      var idField = user.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(user, UUID.randomUUID());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

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
    Url result = urlService.createShortUrl(originalUrl, null, null, null, null, null, null, user);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getOriginalUrl()).isEqualTo(originalUrl);
    assertThat(result.getShortCode()).isEqualTo(shortCode);
    assertThat(result.isActive()).isTrue();
  }

  @Test
  void createShortUrl_WithExpiration_SetsExpirationDate() {
    // Given
    String originalUrl = "https://example.com";
    String customCode = "custom123";
    LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

    when(urlUtils.isValidUrl(originalUrl)).thenReturn(true);
    when(urlUtils.normalizeUrl(originalUrl)).thenReturn(originalUrl);
    when(urlRepository.existsByShortCode(customCode)).thenReturn(false);
    when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Url result =
        urlService.createShortUrl(originalUrl, customCode, null, expiresAt, null, null, null, user);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
    assertThat(result.getShortCode()).isEqualTo(customCode);
    verify(urlRepository).save(any(Url.class));
  }

  @Test
  void createShortUrl_WithClickLimit_SetsClickLimit() {
    // Given
    String originalUrl = "https://example.com";
    Integer clickLimit = 100;

    when(urlUtils.isValidUrl(originalUrl)).thenReturn(true);
    when(urlUtils.normalizeUrl(originalUrl)).thenReturn(originalUrl);
    when(urlUtils.generateShortCode()).thenReturn("abc123");
    when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
    when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Url result =
        urlService.createShortUrl(originalUrl, null, null, null, clickLimit, null, null, user);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getClickLimit()).isEqualTo(clickLimit);
    verify(urlRepository).save(any(Url.class));
  }

  @Test
  void createShortUrl_WithPassword_SetsPasswordProtection() {
    // Given
    String originalUrl = "https://example.com";
    String password = "secret123";
    String hashedPassword = "hashedSecret123";

    when(urlUtils.isValidUrl(originalUrl)).thenReturn(true);
    when(urlUtils.normalizeUrl(originalUrl)).thenReturn(originalUrl);
    when(urlUtils.generateShortCode()).thenReturn("abc123");
    when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
    when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
    when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Url result =
        urlService.createShortUrl(originalUrl, null, null, null, null, null, password, user);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.isPasswordProtected()).isTrue();
    assertThat(result.getPasswordHash()).isEqualTo(hashedPassword);
    verify(passwordEncoder).encode(password);
    verify(urlRepository).save(any(Url.class));
  }

  @Test
  void createShortUrl_InvalidUrl_ThrowsValidationException() {
    // Given
    String invalidUrl = "not-a-url";
    when(urlUtils.isValidUrl(anyString())).thenReturn(false);

    // When & Then
    assertThatThrownBy(
            () -> urlService.createShortUrl(invalidUrl, null, null, null, null, null, null, user))
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
        () ->
            urlService.createShortUrl(
                "https://another.com", customCode, null, null, null, null, null, user));
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
  void verifyUrlPassword_CorrectPassword_ReturnsTrue() {
    // Given
    String shortCode = "abc123";
    String password = "secret123";
    String hashedPassword = "hashedSecret123";

    Url url = new Url("https://example.com", shortCode, user);
    url.setPasswordProtected(true);
    url.setPasswordHash(hashedPassword);

    when(urlRepository.findByShortCodeAndActiveTrue(shortCode)).thenReturn(Optional.of(url));
    when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);

    // When
    boolean result = urlService.verifyUrlPassword(shortCode, password);

    // Then
    assertThat(result).isTrue();
    verify(passwordEncoder).matches(password, hashedPassword);
  }

  @Test
  void verifyUrlPassword_IncorrectPassword_ReturnsFalse() {
    // Given
    String shortCode = "abc123";
    String password = "wrongpassword";
    String hashedPassword = "hashedSecret123";

    Url url = new Url("https://example.com", shortCode, user);
    url.setPasswordProtected(true);
    url.setPasswordHash(hashedPassword);

    when(urlRepository.findByShortCodeAndActiveTrue(shortCode)).thenReturn(Optional.of(url));
    when(passwordEncoder.matches(password, hashedPassword)).thenReturn(false);

    // When
    boolean result = urlService.verifyUrlPassword(shortCode, password);

    // Then
    assertThat(result).isFalse();
    verify(passwordEncoder).matches(password, hashedPassword);
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
