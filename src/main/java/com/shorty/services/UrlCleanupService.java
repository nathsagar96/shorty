package com.shorty.services;

import com.shorty.repositories.UrlMappingRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlCleanupService {

    private final UrlMappingRepository repository;

    @Scheduled(cron = "${app.cleanup.cron:0 0 0 * * ?}")
    @Transactional
    public void cleanupExpiredUrls() {
        log.info("Starting cleanup of expired URLs");

        long startTime = System.currentTimeMillis();
        Instant now = Instant.now();

        try {
            long expiredCount = repository.countExpiredMappings(now);

            if (expiredCount == 0) {
                log.info("No expired URLs to clean up");
                return;
            }

            log.info("Found {} expired URL(s) to delete", expiredCount);

            int deletedCount = repository.deleteExpiredMappings(now);

            long duration = System.currentTimeMillis() - startTime;

            log.info("Cleanup completed: {} URL(s) deleted in {} ms", deletedCount, duration);

            if (deletedCount != expiredCount) {
                log.warn("Mismatch in cleanup: expected {}, deleted {}", expiredCount, deletedCount);
            }

        } catch (Exception e) {
            log.error("Error during cleanup of expired URLs", e);
        }
    }
}
