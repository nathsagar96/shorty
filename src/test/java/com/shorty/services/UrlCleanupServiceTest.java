package com.shorty.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.shorty.repositories.UrlMappingRepository;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlCleanupServiceTest {

    @Mock
    private UrlMappingRepository repository;

    @InjectMocks
    private UrlCleanupService urlCleanupService;

    @Nested
    @DisplayName("Cleanup Expired URLs Tests")
    class CleanupExpiredUrlsTests {

        @Test
        @DisplayName("Should cleanup expired URLs successfully")
        void shouldCleanupExpiredUrlsSuccessfully() {
            // Given
            long expiredCount = 5L;
            int deletedCount = 5;

            when(repository.countExpiredMappings(any(Instant.class))).thenReturn(expiredCount);
            when(repository.deleteExpiredMappings(any(Instant.class))).thenReturn(deletedCount);

            // When
            assertDoesNotThrow(() -> urlCleanupService.cleanupExpiredUrls());

            // Then
            verify(repository, times(1)).countExpiredMappings(any(Instant.class));
            verify(repository, times(1)).deleteExpiredMappings(any(Instant.class));
        }

        @Test
        @DisplayName("Should handle no expired URLs gracefully")
        void shouldHandleNoExpiredUrlsGracefully() {
            // Given
            long expiredCount = 0L;

            when(repository.countExpiredMappings(any(Instant.class))).thenReturn(expiredCount);

            // When
            assertDoesNotThrow(() -> urlCleanupService.cleanupExpiredUrls());

            // Then
            verify(repository, times(1)).countExpiredMappings(any(Instant.class));
            verify(repository, never()).deleteExpiredMappings(any());
        }

        @Test
        @DisplayName("Should handle mismatch between count and delete")
        void shouldHandleMismatchBetweenCountAndDelete() {
            // Given
            long expiredCount = 10L;
            int deletedCount = 8;

            when(repository.countExpiredMappings(any(Instant.class))).thenReturn(expiredCount);
            when(repository.deleteExpiredMappings(any(Instant.class))).thenReturn(deletedCount);

            // When
            assertDoesNotThrow(() -> urlCleanupService.cleanupExpiredUrls());

            // Then
            verify(repository, times(1)).countExpiredMappings(any(Instant.class));
            verify(repository, times(1)).deleteExpiredMappings(any(Instant.class));
        }

        @Test
        @DisplayName("Should handle repository exception gracefully")
        void shouldHandleRepositoryExceptionGracefully() {
            // Given
            when(repository.countExpiredMappings(any(Instant.class))).thenThrow(new RuntimeException("Database error"));

            // When
            assertDoesNotThrow(() -> urlCleanupService.cleanupExpiredUrls());

            // Then
            verify(repository, times(1)).countExpiredMappings(any(Instant.class));
            verify(repository, never()).deleteExpiredMappings(any());
        }

        @Test
        @DisplayName("Should handle exception during deletion gracefully")
        void shouldHandleExceptionDuringDeletionGracefully() {
            // Given
            long expiredCount = 5L;

            when(repository.countExpiredMappings(any(Instant.class))).thenReturn(expiredCount);
            when(repository.deleteExpiredMappings(any(Instant.class)))
                    .thenThrow(new RuntimeException("Deletion error"));

            // When
            assertDoesNotThrow(() -> urlCleanupService.cleanupExpiredUrls());

            // Then
            verify(repository, times(1)).countExpiredMappings(any(Instant.class));
            verify(repository, times(1)).deleteExpiredMappings(any(Instant.class));
        }
    }

    @Nested
    @DisplayName("Concurrent Operations Tests")
    class ConcurrentOperationsTests {

        @Test
        @DisplayName("Should handle concurrent cleanup operations")
        void shouldHandleConcurrentCleanupOperations() throws InterruptedException {
            // Given
            long expiredCount = 10L;
            int deletedCount = 10;

            when(repository.countExpiredMappings(any(Instant.class))).thenReturn(expiredCount);
            when(repository.deleteExpiredMappings(any(Instant.class))).thenReturn(deletedCount);

            // When
            Runnable task = () -> urlCleanupService.cleanupExpiredUrls();

            Thread thread1 = new Thread(task);
            Thread thread2 = new Thread(task);

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            // Then - Both should complete without exceptions
            verify(repository, times(2)).countExpiredMappings(any(Instant.class));
            verify(repository, times(2)).deleteExpiredMappings(any(Instant.class));
        }
    }
}
