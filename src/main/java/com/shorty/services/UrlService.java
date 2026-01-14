package com.shorty.services;

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
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Value("${app.url-expiration.default-hours:8760}")
    private int defaultExpirationHours;

    @Transactional(readOnly = true)
    public PageResponse<UrlResponse> getAllUrls(int page, int size, UUID userId) {
        log.info("Getting all URLs for user ID: {}", userId);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var mappings = repository.findByUserId(pageRequest, userId);

        return new PageResponse<>(
                mappings.stream()
                        .map(mapping -> mapper.toResponse(mapping, baseUrl))
                        .toList(),
                mappings.getNumber(),
                mappings.getSize(),
                mappings.getTotalElements(),
                mappings.getTotalPages(),
                mappings.isFirst(),
                mappings.isLast());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UrlResponse createShortUrl(CreateUrlRequest request, UUID userId) {
        log.info("Creating short URL for: {} with user ID: {}", request.originalUrl(), userId);

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
                .expiresAt(calculateDefaultExpirationTime(request))
                .userId(userId)
                .build();

        UrlMapping saved = repository.save(mapping);
        log.info("Short URL created successfully: {} for user: {}", shortCode, userId);
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
    public UrlResponse getUrlDetails(String shortCode, UUID userId) {
        UrlMapping mapping = repository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

        if (!mapping.getUserId().equals(userId)) {
            throw new UrlNotFoundException("Short URL not found: " + shortCode);
        }

        return mapper.toResponse(mapping, baseUrl);
    }

    @Transactional
    public void deleteShortUrl(String shortCode, UUID userId) {
        UrlMapping mapping = repository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

        if (!mapping.getUserId().equals(userId)) {
            throw new UrlNotFoundException("Short URL not found: " + shortCode);
        }

        repository.delete(mapping);
        log.info("Short URL deleted: {} by user: {}", shortCode, userId);
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

    private Instant calculateDefaultExpirationTime(CreateUrlRequest request) {
        if (request.expirationHours() != null) {
            return Instant.now().plusSeconds(request.expirationHours() * 3600L);
        } else {
            return Instant.now().plusSeconds(defaultExpirationHours * 3600L);
        }
    }
}
