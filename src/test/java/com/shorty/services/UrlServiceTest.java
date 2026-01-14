package com.shorty.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.shorty.dtos.requests.CreateUrlRequest;
import com.shorty.dtos.responses.PageResponse;
import com.shorty.dtos.responses.RedirectResponse;
import com.shorty.dtos.responses.UrlResponse;
import com.shorty.entities.UrlMapping;
import com.shorty.exceptions.AliasAlreadyExistsException;
import com.shorty.exceptions.UrlExpiredException;
import com.shorty.exceptions.UrlNotFoundException;
import com.shorty.mappers.UrlMapper;
import com.shorty.repositories.UrlMappingRepository;
import com.shorty.utils.ShortCodeGenerator;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlMappingRepository repository;

    @Mock
    private ShortCodeGenerator codeGenerator;

    @Mock
    private UrlMapper mapper;

    @InjectMocks
    private UrlService urlService;

    private final String baseUrl = "http://localhost:8080";
    private final int maxRetryAttempts = 3;

    @BeforeEach
    void setUp() {

        try {
            Field baseUrlField = UrlService.class.getDeclaredField("baseUrl");
            baseUrlField.setAccessible(true);
            baseUrlField.set(urlService, baseUrl);

            Field maxRetryField = UrlService.class.getDeclaredField("maxRetryAttempts");
            maxRetryField.setAccessible(true);
            maxRetryField.set(urlService, maxRetryAttempts);

            Field defaultExpirationField = UrlService.class.getDeclaredField("defaultExpirationHours");
            defaultExpirationField.setAccessible(true);
            defaultExpirationField.set(urlService, 8760); // 1 year in hours
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test fields", e);
        }
    }

    @Nested
    @DisplayName("Create Short URL Tests")
    class CreateShortUrlTests {

        @Test
        @DisplayName("Should create short URL when valid input")
        void shouldCreateShortUrlWhenValidInput() {
            // Given
            CreateUrlRequest request = new CreateUrlRequest("https://example.com", null, null);
            String shortCode = "abc123";
            Instant expectedExpiration = Instant.now().plus(8760, ChronoUnit.HOURS);
            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(shortCode)
                    .originalUrl("https://example.com")
                    .expiresAt(expectedExpiration)
                    .build();
            UrlResponse expectedResponse = new UrlResponse(
                    UUID.randomUUID(),
                    "abc123",
                    "http://localhost:8080/abc123",
                    "https://example.com",
                    0L,
                    expectedExpiration,
                    Instant.now());

            when(codeGenerator.generate()).thenReturn(shortCode);
            when(repository.existsByShortCode(shortCode)).thenReturn(false);
            when(repository.save(any(UrlMapping.class))).thenReturn(mapping);
            when(mapper.toResponse(mapping, baseUrl)).thenReturn(expectedResponse);

            // When
            UrlResponse response = urlService.createShortUrl(request, UUID.randomUUID());

            // Then
            assertNotNull(response);
            assertEquals(expectedResponse.shortUrl(), response.shortUrl());
            assertEquals(expectedResponse.originalUrl(), response.originalUrl());
            verify(repository, times(1)).save(any(UrlMapping.class));
            verify(codeGenerator, times(1)).generate();
        }

        @Test
        @DisplayName("Should create short URL with custom alias when valid")
        void shouldCreateShortUrlWithCustomAliasWhenValid() {
            // Given
            String customAlias = "myalias";
            CreateUrlRequest request = new CreateUrlRequest("https://example.com", customAlias, null);
            Instant expectedExpiration = Instant.now().plus(8760, ChronoUnit.HOURS);
            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(customAlias)
                    .originalUrl("https://example.com")
                    .expiresAt(expectedExpiration)
                    .build();
            UrlResponse expectedResponse = new UrlResponse(
                    UUID.randomUUID(),
                    "myalias",
                    "http://localhost:8080/myalias",
                    "https://example.com",
                    0L,
                    expectedExpiration,
                    Instant.now());

            when(codeGenerator.isValidAlias(customAlias)).thenReturn(true);
            when(repository.existsByShortCode(customAlias)).thenReturn(false);
            when(repository.save(any(UrlMapping.class))).thenReturn(mapping);
            when(mapper.toResponse(mapping, baseUrl)).thenReturn(expectedResponse);

            // When
            UrlResponse response = urlService.createShortUrl(request, UUID.randomUUID());

            // Then
            assertNotNull(response);
            assertEquals(expectedResponse.shortUrl(), response.shortUrl());
            assertEquals(expectedResponse.originalUrl(), response.originalUrl());
            verify(repository, times(1)).save(any(UrlMapping.class));
            verify(codeGenerator, never()).generate();
        }

        @Test
        @DisplayName("Should throw exception when invalid custom alias")
        void shouldThrowExceptionWhenInvalidCustomAlias() {
            // Given
            String invalidAlias = "invalid alias";
            CreateUrlRequest request = new CreateUrlRequest("https://example.com", invalidAlias, null);

            when(codeGenerator.isValidAlias(invalidAlias)).thenReturn(false);

            // When/Then
            assertThrows(IllegalArgumentException.class, () -> urlService.createShortUrl(request, UUID.randomUUID()));
            verify(codeGenerator, times(1)).isValidAlias(invalidAlias);
            verify(repository, never()).save(any(UrlMapping.class));
        }

        @Test
        @DisplayName("Should throw exception when alias already exists")
        void shouldThrowExceptionWhenAliasAlreadyExists() {
            // Given
            String existingAlias = "existing";
            CreateUrlRequest request = new CreateUrlRequest("https://example.com", existingAlias, null);

            when(codeGenerator.isValidAlias(existingAlias)).thenReturn(true);
            when(repository.existsByShortCode(existingAlias)).thenReturn(true);

            // When/Then
            assertThrows(
                    AliasAlreadyExistsException.class, () -> urlService.createShortUrl(request, UUID.randomUUID()));
            verify(repository, times(1)).existsByShortCode(existingAlias);
            verify(repository, never()).save(any(UrlMapping.class));
        }

        @Test
        @DisplayName("Should throw exception when max retry attempts exceeded")
        void shouldThrowExceptionWhenMaxRetryAttemptsExceeded() {
            // Given
            CreateUrlRequest request = new CreateUrlRequest("https://example.com", null, null);
            String collidingCode = "collide";

            when(codeGenerator.generate()).thenReturn(collidingCode);
            when(repository.existsByShortCode(collidingCode)).thenReturn(true);

            // When/Then
            assertThrows(IllegalStateException.class, () -> urlService.createShortUrl(request, UUID.randomUUID()));
            verify(codeGenerator, times(maxRetryAttempts)).generate();
            verify(repository, times(maxRetryAttempts)).existsByShortCode(collidingCode);
            verify(repository, never()).save(any(UrlMapping.class));
        }
    }

    @Nested
    @DisplayName("Resolve and Track Tests")
    class ResolveAndTrackTests {

        @Test
        @DisplayName("Should resolve and track valid short code")
        void shouldResolveAndTrackValidShortCode() {
            // Given
            String shortCode = "valid123";
            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(shortCode)
                    .originalUrl("https://example.com")
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .clickCount(0L)
                    .build();
            RedirectResponse expectedResponse = new RedirectResponse("https://example.com", 1L);

            when(repository.findByShortCodeForUpdate(shortCode)).thenReturn(Optional.of(mapping));
            when(repository.save(any(UrlMapping.class))).thenReturn(mapping);

            // When
            RedirectResponse response = urlService.resolveAndTrack(shortCode);

            // Then
            assertNotNull(response);
            assertEquals(expectedResponse.originalUrl(), response.originalUrl());
            assertEquals(1, response.clickCount());
            verify(repository, times(1)).save(mapping);
        }

        @Test
        @DisplayName("Should throw exception when URL not found")
        void shouldThrowExceptionWhenUrlNotFound() {
            // Given
            String nonExistentCode = "nonexist";

            when(repository.findByShortCodeForUpdate(nonExistentCode)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(UrlNotFoundException.class, () -> urlService.resolveAndTrack(nonExistentCode));
            verify(repository, never()).save(any(UrlMapping.class));
        }

        @Test
        @DisplayName("Should throw exception when URL expired")
        void shouldThrowExceptionWhenUrlExpired() {
            // Given
            String expiredCode = "expired";
            UrlMapping expiredMapping = UrlMapping.builder()
                    .shortCode(expiredCode)
                    .originalUrl("https://example.com")
                    .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                    .clickCount(0L)
                    .build();

            when(repository.findByShortCodeForUpdate(expiredCode)).thenReturn(Optional.of(expiredMapping));

            // When/Then
            assertThrows(UrlExpiredException.class, () -> urlService.resolveAndTrack(expiredCode));
            verify(repository, never()).save(any(UrlMapping.class));
        }
    }

    @Nested
    @DisplayName("Get URL Details Tests")
    class GetUrlDetailsTests {

        @Test
        @DisplayName("Should get URL details when exists")
        void shouldGetUrlDetailsWhenExists() {
            // Given
            String shortCode = "details123";
            UUID userId = UUID.randomUUID();
            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(shortCode)
                    .originalUrl("https://example.com")
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .clickCount(5L)
                    .userId(userId)
                    .build();
            UrlResponse expectedResponse = new UrlResponse(
                    UUID.randomUUID(),
                    "details123",
                    "http://localhost:8080/details123",
                    "https://example.com",
                    5L,
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    Instant.now());

            when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(mapping));
            when(mapper.toResponse(mapping, baseUrl)).thenReturn(expectedResponse);

            // When
            UrlResponse response = urlService.getUrlDetails(shortCode, userId);

            // Then
            assertNotNull(response);
            assertEquals(expectedResponse.shortUrl(), response.shortUrl());
            assertEquals(expectedResponse.originalUrl(), response.originalUrl());
            assertEquals(5, response.clickCount());
        }

        @Test
        @DisplayName("Should throw exception when URL not found for details")
        void shouldThrowExceptionWhenUrlNotFoundForDetails() {
            // Given
            String nonExistentCode = "nonexist";

            when(repository.findByShortCode(nonExistentCode)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(
                    UrlNotFoundException.class, () -> urlService.getUrlDetails(nonExistentCode, UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("Delete Short URL Tests")
    class DeleteShortUrlTests {

        @Test
        @DisplayName("Should delete short URL when exists")
        void shouldDeleteShortUrlWhenExists() {
            // Given
            String shortCode = "delete123";
            UUID userId = UUID.randomUUID();
            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(shortCode)
                    .originalUrl("https://example.com")
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .userId(userId)
                    .build();

            when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(mapping));

            // When
            assertDoesNotThrow(() -> urlService.deleteShortUrl(shortCode, userId));

            // Then
            verify(repository, times(1)).delete(mapping);
        }

        @Test
        @DisplayName("Should throw exception when URL not found for deletion")
        void shouldThrowExceptionWhenUrlNotFoundForDeletion() {
            // Given
            String nonExistentCode = "nonexist";

            when(repository.findByShortCode(nonExistentCode)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(
                    UrlNotFoundException.class, () -> urlService.deleteShortUrl(nonExistentCode, UUID.randomUUID()));
            verify(repository, never()).delete(any(UrlMapping.class));
        }
    }

    @Nested
    @DisplayName("Concurrent Operations Tests")
    class ConcurrentOperationsTests {

        @Test
        @DisplayName("Should handle concurrent URL creation")
        void shouldHandleConcurrentUrlCreation() throws InterruptedException {
            // Given
            CreateUrlRequest request = new CreateUrlRequest("https://example.com", null, null);
            String shortCode = "concurrent123";
            Instant expectedExpiration = Instant.now().plus(8760, ChronoUnit.HOURS);

            when(codeGenerator.generate()).thenReturn(shortCode);
            when(repository.existsByShortCode(shortCode)).thenReturn(false);
            when(repository.save(any(UrlMapping.class)))
                    .thenReturn(UrlMapping.builder()
                            .shortCode(shortCode)
                            .originalUrl("https://example.com")
                            .expiresAt(expectedExpiration)
                            .build());

            // When
            Runnable task = () -> urlService.createShortUrl(request, UUID.randomUUID());

            Thread thread1 = new Thread(task);
            Thread thread2 = new Thread(task);

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            // Then - At least one should succeed, the other should handle collision
            verify(repository, atLeast(1)).save(any(UrlMapping.class));
            verify(codeGenerator, atLeast(1)).generate();
        }
    }

    @Nested
    @DisplayName("User Authorization Tests")
    class UserAuthorizationTests {

        @Test
        @DisplayName("Should throw exception when user ID mismatch in getUrlDetails")
        void shouldThrowExceptionWhenUserIdMismatchInGetDetails() {
            // Given
            String shortCode = "test123";
            UUID ownerId = UUID.randomUUID();
            UUID differentUserId = UUID.randomUUID();

            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(shortCode)
                    .originalUrl("https://example.com")
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .userId(ownerId)
                    .build();

            when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(mapping));

            // When/Then
            assertThrows(UrlNotFoundException.class, () -> urlService.getUrlDetails(shortCode, differentUserId));
            verify(repository, times(1)).findByShortCode(shortCode);
        }

        @Test
        @DisplayName("Should throw exception when user ID mismatch in deleteShortUrl")
        void shouldThrowExceptionWhenUserIdMismatchInDelete() {
            // Given
            String shortCode = "test123";
            UUID ownerId = UUID.randomUUID();
            UUID differentUserId = UUID.randomUUID();

            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(shortCode)
                    .originalUrl("https://example.com")
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .userId(ownerId)
                    .build();

            when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(mapping));

            // When/Then
            assertThrows(UrlNotFoundException.class, () -> urlService.deleteShortUrl(shortCode, differentUserId));
            verify(repository, times(1)).findByShortCode(shortCode);
            verify(repository, never()).delete(any(UrlMapping.class));
        }
    }

    @Nested
    @DisplayName("Expiration Logic Tests")
    class ExpirationLogicTests {

        @Test
        @DisplayName("Should use custom expiration time when provided")
        void shouldUseCustomExpirationTime() {
            // Given
            int customExpirationHours = 24;
            CreateUrlRequest request = new CreateUrlRequest("https://example.com", null, customExpirationHours);
            String shortCode = "customExp";
            Instant expectedExpiration = Instant.now().plusSeconds(customExpirationHours * 3600L);

            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(shortCode)
                    .originalUrl("https://example.com")
                    .expiresAt(expectedExpiration)
                    .build();

            UrlResponse expectedResponse = new UrlResponse(
                    UUID.randomUUID(),
                    shortCode,
                    "http://localhost:8080/" + shortCode,
                    "https://example.com",
                    0L,
                    expectedExpiration,
                    Instant.now());

            when(codeGenerator.generate()).thenReturn(shortCode);
            when(repository.existsByShortCode(shortCode)).thenReturn(false);
            when(repository.save(any(UrlMapping.class))).thenReturn(mapping);
            when(mapper.toResponse(mapping, baseUrl)).thenReturn(expectedResponse);

            // When
            UrlResponse response = urlService.createShortUrl(request, UUID.randomUUID());

            // Then
            assertNotNull(response);
            assertEquals(expectedResponse.shortUrl(), response.shortUrl());
            verify(repository, times(1)).save(any(UrlMapping.class));
        }

        @Test
        @DisplayName("Should use default expiration time when not provided")
        void shouldUseDefaultExpirationTime() {
            // Given
            CreateUrlRequest request = new CreateUrlRequest("https://example.com", null, null);
            String shortCode = "defaultExp";
            Instant expectedExpiration = Instant.now().plusSeconds(8760 * 3600L); // 1 year

            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(shortCode)
                    .originalUrl("https://example.com")
                    .expiresAt(expectedExpiration)
                    .build();

            UrlResponse expectedResponse = new UrlResponse(
                    UUID.randomUUID(),
                    shortCode,
                    "http://localhost:8080/" + shortCode,
                    "https://example.com",
                    0L,
                    expectedExpiration,
                    Instant.now());

            when(codeGenerator.generate()).thenReturn(shortCode);
            when(repository.existsByShortCode(shortCode)).thenReturn(false);
            when(repository.save(any(UrlMapping.class))).thenReturn(mapping);
            when(mapper.toResponse(mapping, baseUrl)).thenReturn(expectedResponse);

            // When
            UrlResponse response = urlService.createShortUrl(request, UUID.randomUUID());

            // Then
            assertNotNull(response);
            assertEquals(expectedResponse.shortUrl(), response.shortUrl());
            verify(repository, times(1)).save(any(UrlMapping.class));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long URLs")
        void shouldHandleVeryLongUrls() {
            // Given
            String longUrl = "https://example.com/" + "a".repeat(2000);
            CreateUrlRequest request = new CreateUrlRequest(longUrl, null, null);
            String shortCode = "longUrl";

            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(shortCode)
                    .originalUrl(longUrl)
                    .expiresAt(Instant.now().plusSeconds(8760 * 3600L))
                    .build();

            UrlResponse expectedResponse = new UrlResponse(
                    UUID.randomUUID(),
                    shortCode,
                    "http://localhost:8080/" + shortCode,
                    longUrl,
                    0L,
                    Instant.now().plusSeconds(8760 * 3600L),
                    Instant.now());

            when(codeGenerator.generate()).thenReturn(shortCode);
            when(repository.existsByShortCode(shortCode)).thenReturn(false);
            when(repository.save(any(UrlMapping.class))).thenReturn(mapping);
            when(mapper.toResponse(mapping, baseUrl)).thenReturn(expectedResponse);

            // When
            UrlResponse response = urlService.createShortUrl(request, UUID.randomUUID());

            // Then
            assertNotNull(response);
            assertEquals(expectedResponse.shortUrl(), response.shortUrl());
            assertEquals(longUrl, response.originalUrl());
        }

        @Test
        @DisplayName("Should handle URLs with special characters")
        void shouldHandleUrlsWithSpecialCharacters() {
            // Given
            String urlWithSpecialChars = "https://example.com/path?param=value&other=test#fragment";
            CreateUrlRequest request = new CreateUrlRequest(urlWithSpecialChars, null, null);
            String shortCode = "specialChars";

            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(shortCode)
                    .originalUrl(urlWithSpecialChars)
                    .expiresAt(Instant.now().plusSeconds(8760 * 3600L))
                    .build();

            UrlResponse expectedResponse = new UrlResponse(
                    UUID.randomUUID(),
                    shortCode,
                    "http://localhost:8080/" + shortCode,
                    urlWithSpecialChars,
                    0L,
                    Instant.now().plusSeconds(8760 * 3600L),
                    Instant.now());

            when(codeGenerator.generate()).thenReturn(shortCode);
            when(repository.existsByShortCode(shortCode)).thenReturn(false);
            when(repository.save(any(UrlMapping.class))).thenReturn(mapping);
            when(mapper.toResponse(mapping, baseUrl)).thenReturn(expectedResponse);

            // When
            UrlResponse response = urlService.createShortUrl(request, UUID.randomUUID());

            // Then
            assertNotNull(response);
            assertEquals(expectedResponse.shortUrl(), response.shortUrl());
            assertEquals(urlWithSpecialChars, response.originalUrl());
        }

        @Test
        @DisplayName("Should handle case sensitivity in short codes")
        void shouldHandleCaseSensitivityInShortCodes() {
            // Given
            String shortCode = "Test123";
            String shortCodeLower = "test123";
            UrlMapping mapping = UrlMapping.builder()
                    .shortCode(shortCode)
                    .originalUrl("https://example.com")
                    .expiresAt(Instant.now().plusSeconds(8760 * 3600L))
                    .userId(UUID.randomUUID())
                    .build();

            when(repository.findByShortCodeForUpdate(shortCode)).thenReturn(Optional.of(mapping));
            when(repository.findByShortCodeForUpdate(shortCodeLower)).thenReturn(Optional.empty());

            // When/Then - Original case should work
            assertDoesNotThrow(() -> urlService.resolveAndTrack(shortCode));

            // When/Then - Different case should fail
            assertThrows(UrlNotFoundException.class, () -> urlService.resolveAndTrack(shortCodeLower));
        }
    }

    @Nested
    @DisplayName("Get All URLs Tests")
    class GetAllUrlsTests {

        @Test
        @DisplayName("Should return empty page when no URLs exist")
        void shouldReturnEmptyPageWhenNoUrlsExist() {
            // Given
            int page = 0;
            int size = 10;
            UUID userId = UUID.randomUUID();

            Page<UrlMapping> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
            when(repository.findByUserId(any(Pageable.class), eq(userId))).thenReturn(emptyPage);

            // When
            PageResponse<UrlResponse> response = urlService.getAllUrls(page, size, userId);

            // Then
            assertNotNull(response);
            assertTrue(response.content().isEmpty());
            assertEquals(0, response.totalElements());
            assertEquals(0, response.totalPages());
            assertTrue(response.first());
            assertTrue(response.last());
        }

        @Test
        @DisplayName("Should return paginated URLs when URLs exist")
        void shouldReturnPaginatedUrlsWhenUrlsExist() {
            // Given
            int page = 0;
            int size = 5;
            UUID userId = UUID.randomUUID();

            // Create test URL mappings
            UrlMapping mapping1 = UrlMapping.builder()
                    .shortCode("test1")
                    .originalUrl("https://example1.com")
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .userId(userId)
                    .build();

            UrlMapping mapping2 = UrlMapping.builder()
                    .shortCode("test2")
                    .originalUrl("https://example2.com")
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .userId(userId)
                    .build();

            // Create expected responses
            UrlResponse response1 = new UrlResponse(
                    UUID.randomUUID(),
                    "test1",
                    "http://localhost:8080/test1",
                    "https://example1.com",
                    0L,
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    Instant.now());

            UrlResponse response2 = new UrlResponse(
                    UUID.randomUUID(),
                    "test2",
                    "http://localhost:8080/test2",
                    "https://example2.com",
                    0L,
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    Instant.now());

            // Mock the repository to return a page with 2 items
            List<UrlMapping> content = new ArrayList<>();
            content.add(mapping1);
            content.add(mapping2);
            Page<UrlMapping> mockPage = new PageImpl<>(content, PageRequest.of(page, size), 2);

            when(repository.findByUserId(any(Pageable.class), eq(userId))).thenReturn(mockPage);
            when(mapper.toResponse(mapping1, baseUrl)).thenReturn(response1);
            when(mapper.toResponse(mapping2, baseUrl)).thenReturn(response2);

            // When
            PageResponse<UrlResponse> response = urlService.getAllUrls(page, size, userId);

            // Then
            assertNotNull(response);
            assertEquals(2, response.content().size());
            assertEquals(2, response.totalElements());
            assertEquals(1, response.totalPages());
            assertTrue(response.first());
            assertTrue(response.last());
            assertEquals(response1.shortUrl(), response.content().get(0).shortUrl());
            assertEquals(response2.shortUrl(), response.content().get(1).shortUrl());
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void shouldHandlePaginationCorrectly() {
            // Given
            int page = 1; // Second page
            int size = 2;
            UUID userId = UUID.randomUUID();

            // Create more test URL mappings than page size
            UrlMapping mapping1 = UrlMapping.builder()
                    .shortCode("page2-1")
                    .originalUrl("https://page2-example1.com")
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .userId(userId)
                    .build();

            UrlMapping mapping2 = UrlMapping.builder()
                    .shortCode("page2-2")
                    .originalUrl("https://page2-example2.com")
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .userId(userId)
                    .build();

            // Mock the repository to return a page with items for page 1
            List<UrlMapping> content = new ArrayList<>();
            content.add(mapping1);
            content.add(mapping2);
            Page<UrlMapping> mockPage = new PageImpl<>(content, PageRequest.of(page, size), 5); // Total 5 items

            when(repository.findByUserId(any(Pageable.class), eq(userId))).thenReturn(mockPage);
            when(mapper.toResponse(any(UrlMapping.class), eq(baseUrl))).thenAnswer(invocation -> {
                UrlMapping mapping = invocation.getArgument(0);
                return new UrlResponse(
                        UUID.randomUUID(),
                        mapping.getShortCode(),
                        "http://localhost:8080/" + mapping.getShortCode(),
                        mapping.getOriginalUrl(),
                        0L,
                        mapping.getExpiresAt(),
                        Instant.now());
            });

            // When
            PageResponse<UrlResponse> response = urlService.getAllUrls(page, size, userId);

            // Then
            assertNotNull(response);
            assertEquals(2, response.content().size());
            assertEquals(5, response.totalElements());
            assertEquals(3, response.totalPages()); // 5 items / 2 per page = 3 pages
            assertFalse(response.first());
            assertFalse(response.last());
            assertEquals(1, response.pageNumber());
            assertEquals(2, response.pageSize());
        }

        @Test
        @DisplayName("Should handle different page sizes")
        void shouldHandleDifferentPageSizes() {
            // Given
            int page = 0;
            int size = 10; // Larger page size
            UUID userId = UUID.randomUUID();

            // Create test URL mappings
            java.util.List<UrlMapping> mappings = new java.util.ArrayList<>();
            for (int i = 0; i < 10; i++) {
                UrlMapping mapping = UrlMapping.builder()
                        .shortCode("test" + i)
                        .originalUrl("https://example" + i + ".com")
                        .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                        .userId(userId)
                        .build();
                mappings.add(mapping);
            }

            // Mock the repository
            Page<UrlMapping> mockPage = new PageImpl<>(mappings, PageRequest.of(page, size), 10);

            when(repository.findByUserId(any(Pageable.class), eq(userId))).thenReturn(mockPage);
            when(mapper.toResponse(any(UrlMapping.class), eq(baseUrl))).thenAnswer(invocation -> {
                UrlMapping mapping = invocation.getArgument(0);
                return new UrlResponse(
                        UUID.randomUUID(),
                        mapping.getShortCode(),
                        "http://localhost:8080/" + mapping.getShortCode(),
                        mapping.getOriginalUrl(),
                        0L,
                        mapping.getExpiresAt(),
                        Instant.now());
            });

            // When
            PageResponse<UrlResponse> response = urlService.getAllUrls(page, size, userId);

            // Then
            assertNotNull(response);
            assertEquals(10, response.content().size());
            assertEquals(10, response.totalElements());
            assertEquals(1, response.totalPages());
            assertTrue(response.first());
            assertTrue(response.last());
            assertEquals(0, response.pageNumber());
            assertEquals(10, response.pageSize());
        }
    }
}
