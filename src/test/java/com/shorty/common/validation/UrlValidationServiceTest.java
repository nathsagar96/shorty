package com.shorty.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.shorty.common.util.UrlUtils;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlValidationServiceTest {

  @Mock private UrlUtils urlUtils;

  @InjectMocks private UrlValidationService validationService;

  @Test
  void validateUrl_ValidHttpsUrl_PassesValidation() {
    // Given
    String url = "https://www.example.com";

    // When
    UrlValidationService.ValidationResult result = validationService.validateUrl(url);

    // Then
    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrors()).isEmpty();
  }

  @Test
  void validateUrl_HttpUrl_HasWarning() {
    // Given
    String url = "http://www.example.com";

    // When
    UrlValidationService.ValidationResult result = validationService.validateUrl(url);

    // Then
    assertThat(result.isValid()).isTrue();
    assertThat(result.getWarnings()).contains("HTTP URLs are less secure than HTTPS");
  }

  @Test
  void validateUrl_JavascriptProtocol_FailsValidation() {
    // Given
    String url = "javascript:alert('xss')";

    // When
    UrlValidationService.ValidationResult result = validationService.validateUrl(url);

    // Then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).anyMatch(error -> error.contains("javascript"));
  }

  @Test
  void validateUrl_LocalhostDomain_FailsValidation() {
    // Given
    String url = "https://localhost/test";

    // When
    UrlValidationService.ValidationResult result = validationService.validateUrl(url);

    // Then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).anyMatch(error -> error.contains("localhost"));
  }

  @Test
  void validateUrl_TooLong_FailsValidation() {
    // Given
    String url = "https://example.com/" + "a".repeat(2050);

    // When
    UrlValidationService.ValidationResult result = validationService.validateUrl(url);

    // Then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).anyMatch(error -> error.contains("too long"));
  }

  @Test
  void validateCustomCode_ValidCode_PassesValidation() {
    // Given
    String customCode = "myCustom123";

    // When
    UrlValidationService.ValidationResult result = validationService.validateCustomCode(customCode);

    // Then
    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrors()).isEmpty();
  }

  @Test
  void validateCustomCode_TooShort_FailsValidation() {
    // Given
    String customCode = "ab";

    // When
    UrlValidationService.ValidationResult result = validationService.validateCustomCode(customCode);

    // Then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).anyMatch(error -> error.contains("at least 3 characters"));
  }

  @Test
  void validateCustomCode_ReservedWord_FailsValidation() {
    // Given
    String customCode = "admin";

    // When
    UrlValidationService.ValidationResult result = validationService.validateCustomCode(customCode);

    // Then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).anyMatch(error -> error.contains("reserved word"));
  }

  @Test
  void validateExpirationDate_FutureDate_PassesValidation() {
    // Given
    LocalDateTime futureDate = LocalDateTime.now().plusDays(7);

    // When
    UrlValidationService.ValidationResult result =
        validationService.validateExpirationDate(futureDate);

    // Then
    assertThat(result.isValid()).isTrue();
    assertThat(result.getErrors()).isEmpty();
  }

  @Test
  void validateExpirationDate_PastDate_FailsValidation() {
    // Given
    LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

    // When
    UrlValidationService.ValidationResult result =
        validationService.validateExpirationDate(pastDate);

    // Then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).anyMatch(error -> error.contains("must be in the future"));
  }

  @Test
  void validateExpirationDate_TooFarInFuture_FailsValidation() {
    // Given
    LocalDateTime farFutureDate = LocalDateTime.now().plusYears(15);

    // When
    UrlValidationService.ValidationResult result =
        validationService.validateExpirationDate(farFutureDate);

    // Then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).anyMatch(error -> error.contains("more than 10 years"));
  }
}
