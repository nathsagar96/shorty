package com.shorty.services;

import com.shorty.dtos.requests.CreateUrlRequest;
import com.shorty.dtos.responses.RedirectResponse;
import com.shorty.dtos.responses.UrlResponse;
import com.shorty.entities.UrlMapping;
import com.shorty.exceptions.AliasAlreadyExistsException;
import com.shorty.exceptions.UrlExpiredException;
import com.shorty.exceptions.UrlNotFoundException;
import com.shorty.mappers.UrlMapper;
import com.shorty.repositories.UrlMappingRepository;
import com.shorty.utils.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlService {

    private final UrlMappingRepository repository;
    private final ShortCodeGenerator codeGenerator;
    private final UrlMapper mapper;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.retry.attempts:3}")
    private int maxRetryAttempts;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UrlResponse createShortUrl(CreateUrlRequest request) {
        log.info("Creating short URL for: {}", request.originalUrl());

        String shortCode;

        if (StringUtils.hasText(request.customAlias())) {
            shortCode = request.customAlias();

            if (!codeGenerator.isValidAlias(shortCode)) {
                throw new IllegalArgumentException("Invalid custom alias format");
            }

            if (repository.existsByShortCode(shortCode)) {
                throw new AliasAlreadyExistsException("Custom alias '" + shortCode + "' is already in use");
            }
        } else {
            shortCode = generateUniqueShortCode();
        }

        UrlMapping mapping = UrlMapping.builder()
                .shortCode(shortCode)
                .originalUrl(request.originalUrl())
                .expiresAt(request.calculateExpirationTime())
                .build();

        UrlMapping saved = repository.save(mapping);
        log.info("Short URL created successfully: {}", shortCode);
        return mapper.toResponse(saved, baseUrl);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RedirectResponse resolveAndTrack(String shortCode) {
        log.debug("Resolving short code: {}", shortCode);

        UrlMapping mapping = repository
                .findByShortCodeForUpdate(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

        if (mapping.isExpired()) {
            log.warn("Attempted to access expired URL: {}", shortCode);
            throw new UrlExpiredException("This short URL has expired on " + mapping.getExpiresAt());
        }

        mapping.incrementClickCount();
        repository.save(mapping);

        log.info("Short code {} resolved. Click count: {}", shortCode, mapping.getClickCount());

        return new RedirectResponse(mapping.getOriginalUrl(), mapping.getClickCount());
    }

    @Transactional(readOnly = true)
    public UrlResponse getUrlDetails(String shortCode) {
        UrlMapping mapping = repository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

        return mapper.toResponse(mapping, baseUrl);
    }

    @Transactional
    public void deleteShortUrl(String shortCode) {
        UrlMapping mapping = repository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

        repository.delete(mapping);
        log.info("Short URL deleted: {}", shortCode);
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < maxRetryAttempts; attempt++) {
            String code = codeGenerator.generate();

            if (!repository.existsByShortCode(code)) {
                return code;
            }

            log.debug("Short code collision on attempt {}: {}", attempt + 1, code);
        }

        throw new IllegalStateException("Failed to generate unique short code after " + maxRetryAttempts + " attempts");
    }
}
