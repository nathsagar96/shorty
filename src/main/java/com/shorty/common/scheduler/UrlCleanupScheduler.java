package com.shorty.common.scheduler;

import com.shorty.urls.UrlService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "app.url.cleanup.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class UrlCleanupScheduler {

  private static final Logger logger = LoggerFactory.getLogger(UrlCleanupScheduler.class);

  private final UrlService urlService;

  @Scheduled(cron = "${app.url.cleanup.cron:0 0 2 * * ?}")
  public void cleanupExpiredUrls() {
    logger.info("Starting expired URL cleanup task");

    try {
      int cleanedUpCount = urlService.cleanupExpiredUrls();
      logger.info("Cleaned up {} expired URLs", cleanedUpCount);
    } catch (Exception e) {
      logger.error("Error during URL cleanup", e);
    }
  }
}
