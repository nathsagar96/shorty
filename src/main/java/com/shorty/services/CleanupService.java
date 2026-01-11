package com.shorty.services;

import com.shorty.repositories.UrlMappingRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {
    private final UrlMappingRepository urlMappingRepository;

    @Transactional
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanupExpiredUrls() {
        log.info("Starting scheduled job: Cleaning up expired URL mappings...");

        long deletedCount = urlMappingRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        if (deletedCount > 0) {
            log.info("Finished scheduled job: Successfully deleted {} expired URL mappings.", deletedCount);
        } else {
            log.info("Finished scheduled job: No expired URL mappings found to delete.");
        }
    }
}
