package com.shorty.urls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.shorty.urls.dto.*;
import com.shorty.urls.dto.BulkCreateUrlRequest;
import com.shorty.urls.dto.BulkError;
import com.shorty.urls.dto.BulkOperationResponse;
import com.shorty.users.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BulkUrlServiceTest {

  private final String baseUrl = "http://localhost:8080";
  @Mock private UrlService urlService;
  @Mock private UrlRepository urlRepository;
  @InjectMocks private BulkUrlService bulkUrlService;
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
  void bulkCreateUrls_ValidUrls_CreatesAll() {
    // Given
    List<CreateUrlRequest> urlRequests =
        List.of(
            new CreateUrlRequest(
                "https://example1.com",
                "abc123",
                UrlVisibility.PUBLIC,
                LocalDateTime.now().plusDays(1),
                -1),
            new CreateUrlRequest(
                "https://example2.com",
                "def456",
                UrlVisibility.PUBLIC,
                LocalDateTime.now().plusDays(1),
                -1));
    BulkCreateUrlRequest request = new BulkCreateUrlRequest(urlRequests);

    Url url1 = new Url("https://example1.com", "abc123", user);
    Url url2 = new Url("https://example2.com", "def456", user);

    when(urlService.createShortUrl(
            anyString(),
            anyString(),
            any(UrlVisibility.class),
            any(LocalDateTime.class),
            anyInt(),
            any(User.class)))
        .thenReturn(url1, url2);

    // When
    BulkOperationResponse<UrlResponse> result =
        bulkUrlService.bulkCreateUrls(request, user, baseUrl);

    // Then
    assertThat(result.totalProcessed()).isEqualTo(2);
    assertThat(result.successCount()).isEqualTo(2);
    assertThat(result.errorCount()).isEqualTo(0);
    assertThat(result.successful()).hasSize(2);
    assertThat(result.errors()).isEmpty();

    verify(urlService, times(2))
        .createShortUrl(
            anyString(),
            anyString(),
            any(UrlVisibility.class),
            any(LocalDateTime.class),
            anyInt(),
            any(User.class));
  }

  @Test
  void bulkCreateUrls_SomeFailures_ReturnsPartialSuccess() {
    // Given
    List<CreateUrlRequest> urlRequests =
        List.of(
            new CreateUrlRequest(
                "https://example1.com",
                "abc123",
                UrlVisibility.PUBLIC,
                LocalDateTime.now().plusDays(1),
                -1),
            new CreateUrlRequest(
                "invalid-url", "def456", UrlVisibility.PUBLIC, LocalDateTime.now().plusDays(1), -1),
            new CreateUrlRequest(
                "https://example3.com",
                "ghi789",
                UrlVisibility.PUBLIC,
                LocalDateTime.now().plusDays(1),
                -1));
    BulkCreateUrlRequest request = new BulkCreateUrlRequest(urlRequests);

    Url url1 = new Url("https://example1.com", "abc123", user);
    Url url3 = new Url("https://example3.com", "ghi789", user);

    when(urlService.createShortUrl(
            anyString(),
            anyString(),
            any(UrlVisibility.class),
            any(LocalDateTime.class),
            anyInt(),
            any(User.class)))
        .thenReturn(url1)
        .thenThrow(new RuntimeException("Invalid URL format"))
        .thenReturn(url3);

    // When
    BulkOperationResponse<UrlResponse> result =
        bulkUrlService.bulkCreateUrls(request, user, baseUrl);

    // Then
    assertThat(result.totalProcessed()).isEqualTo(3);
    assertThat(result.successCount()).isEqualTo(2);
    assertThat(result.errorCount()).isEqualTo(1);
    assertThat(result.successful()).hasSize(2);
    assertThat(result.errors()).hasSize(1);

    BulkError error = result.errors().getFirst();
    assertThat(error.index()).isEqualTo(1);
    assertThat(error.url()).isEqualTo("invalid-url");
    assertThat(error.errorMessage()).isEqualTo("Invalid URL format");
  }

  @Test
  void bulkDeleteUrls_ValidUrls_DeletesAll() {
    // Given
    List<UUID> urlIds =
        List.of(
            UUID.fromString("17fd1334-7557-4b10-bbd5-5ff2d6f5f31b"),
            UUID.fromString("53bff07b-b2e6-4448-810b-6a897bdaf036"));

    Url url1 = new Url("https://example1.com", "abc123", user);
    url1.setUser(user);
    Url url2 = new Url("https://example2.com", "def456", user);
    url2.setUser(user);

    when(urlRepository.findById(UUID.fromString("17fd1334-7557-4b10-bbd5-5ff2d6f5f31b")))
        .thenReturn(java.util.Optional.of(url1));
    when(urlRepository.findById(UUID.fromString("53bff07b-b2e6-4448-810b-6a897bdaf036")))
        .thenReturn(java.util.Optional.of(url2));

    // When
    BulkOperationResponse<UUID> result = bulkUrlService.bulkDeleteUrls(urlIds, user.getId());

    // Then
    assertThat(result.totalProcessed()).isEqualTo(2);
    assertThat(result.successCount()).isEqualTo(2);
    assertThat(result.errorCount()).isEqualTo(0);
    assertThat(result.successful())
        .containsExactly(
            UUID.fromString("17fd1334-7557-4b10-bbd5-5ff2d6f5f31b"),
            UUID.fromString("53bff07b-b2e6-4448-810b-6a897bdaf036"));

    verify(urlRepository).delete(url1);
    verify(urlRepository).delete(url2);
  }
}
