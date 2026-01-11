package com.shorty.services;

import com.shorty.dtos.requests.CreateUrlRequest;
import com.shorty.dtos.responses.UrlResponse;
import com.shorty.dtos.responses.UrlStatsResponse;
import com.shorty.entities.UrlMapping;
import com.shorty.exceptions.AliasAlreadyExistsException;
import com.shorty.exceptions.UrlExpiredException;
import com.shorty.exceptions.UrlNotFoundException;
import com.shorty.mappers.UrlMapper;
import com.shorty.repositories.UrlMappingRepository;
import com.shorty.utils.ShortCodeGenerator;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerService {

    private final UrlMappingRepository urlMappingRepository;
    private final UrlMapper urlMapper;
    private final ShortCodeGenerator shortCodeGenerator;

    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request) {
        String shortCode;

        if (StringUtils.hasText(request.customAlias())) {
            if (urlMappingRepository.existsByShortCode(request.customAlias())) {
                throw new AliasAlreadyExistsException("Custom alias '" + request.customAlias() + "' already exists");
            }
            shortCode = request.customAlias();
        } else {
            shortCode = generateUniqueShortCode();
        }

        UrlMapping urlMapping = UrlMapping.builder()
                .originalUrl(request.url())
                .shortCode(shortCode)
                .build();

        if (request.hoursToExpire() != null) {
            urlMapping.setExpiresAt(LocalDateTime.now().plusHours(request.hoursToExpire()));
        }

        UrlMapping saved = urlMappingRepository.save(urlMapping);
        log.info("Created short URL: {} for {}", shortCode, request.url());

        return urlMapper.toUrlResponse(saved);
    }

    @Transactional
    public String getOriginalUrlAndTrackClick(String shortCode) {
        UrlMapping urlMapping = urlMappingRepository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

        if (urlMapping.getExpiresAt() != null && urlMapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("This link has expired and is no longer active");
        }

        urlMapping.incrementClicks();
        urlMappingRepository.save(urlMapping);

        log.info("Redirecting {} to {} (clicks: {})", shortCode, urlMapping.getOriginalUrl(), urlMapping.getClicks());

        return urlMapping.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    public UrlStatsResponse getUrlStats(String shortCode) {
        UrlMapping urlMapping = urlMappingRepository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

        return urlMapper.toUrlStatsResponse(urlMapping);
    }

    private String generateUniqueShortCode() {
        String shortCode;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            shortCode = shortCodeGenerator.generate();
            attempts++;

            if (attempts > maxAttempts) {
                log.error("Failed to generate unique short code after {} attempts", maxAttempts);
                throw new RuntimeException("Unable to generate unique short code");
            }
        } while (urlMappingRepository.existsByShortCode(shortCode));

        return shortCode;
    }
}
