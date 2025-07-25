package com.shorty.urls;

import com.shorty.common.exception.ResourceNotFoundException;
import com.shorty.common.exception.ValidationException;
import com.shorty.common.util.UrlUtils;
import com.shorty.users.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UrlService {

  private final UrlRepository urlRepository;
  private final UrlUtils urlUtils;

  public UrlService(UrlRepository urlRepository, UrlUtils urlUtils) {
    this.urlRepository = urlRepository;
    this.urlUtils = urlUtils;
  }

  public Url createShortUrl(
      String originalUrl, String customCode, UrlVisibility visibility, User user) {
    if (!urlUtils.isValidUrl(originalUrl)) {
      throw new ValidationException("Invalid URL format");
    }

    String normalizedUrl = urlUtils.normalizeUrl(originalUrl);

    String shortCode =
        customCode != null && !customCode.trim().isEmpty()
            ? customCode.trim()
            : generateUniqueShortCode();

    if (urlRepository.existsByShortCode(shortCode)) {
      throw new ValidationException("Short code already exists: " + shortCode);
    }

    Url url = new Url(normalizedUrl, shortCode, user);
    url.setVisibility(visibility != null ? visibility : UrlVisibility.PUBLIC);

    return urlRepository.save(url);
  }

  public Url createShortUrl(String originalUrl, String customCode) {
    return createShortUrl(originalUrl, customCode, UrlVisibility.PUBLIC, null);
  }

  @Transactional(readOnly = true)
  public Optional<Url> findByShortCode(String shortCode) {
    return urlRepository.findByShortCodeAndActiveTrue(shortCode);
  }

  @Transactional(readOnly = true)
  public Page<Url> getUserUrls(UUID userId, Pageable pageable) {
    return urlRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  @Transactional(readOnly = true)
  public List<Url> getUserUrls(UUID userId) {
    return urlRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  @Transactional(readOnly = true)
  public Page<Url> getPublicUrls(Pageable pageable) {
    return urlRepository.findPublicUrls(pageable);
  }

  public Url updateUrl(UUID urlId, UUID userId, String originalUrl, UrlVisibility visibility) {
    Url url =
        urlRepository
            .findById(urlId)
            .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + urlId));

    if (url.getUser() == null || !url.getUser().getId().equals(userId)) {
      throw new ValidationException("You don't have permission to update this URL");
    }

    if (originalUrl != null && !originalUrl.equals(url.getOriginalUrl())) {
      if (!urlUtils.isValidUrl(originalUrl)) {
        throw new ValidationException("Invalid URL format");
      }
      url.setOriginalUrl(urlUtils.normalizeUrl(originalUrl));
    }

    if (visibility != null) {
      url.setVisibility(visibility);
    }

    return urlRepository.save(url);
  }

  public void deleteUrl(UUID urlId, UUID userId) {
    Url url =
        urlRepository
            .findById(urlId)
            .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + urlId));

    if (url.getUser() == null || !url.getUser().getId().equals(userId)) {
      throw new ValidationException("You don't have permission to delete this URL");
    }

    urlRepository.delete(url);
  }

  public Url toggleUrlStatus(UUID urlId, UUID userId) {
    Url url =
        urlRepository
            .findById(urlId)
            .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + urlId));

    if (url.getUser() == null || !url.getUser().getId().equals(userId)) {
      throw new ValidationException("You don't have permission to modify this URL");
    }

    url.setActive(!url.isActive());
    return urlRepository.save(url);
  }

  @Transactional(readOnly = true)
  public long getUrlCount() {
    return urlRepository.count();
  }

  @Transactional(readOnly = true)
  public long getUserActiveUrlCount(UUID userId) {
    return urlRepository.countActiveUrlsByUserId(userId);
  }

  private String generateUniqueShortCode() {
    String shortCode;
    int attempts = 0;
    do {
      shortCode = urlUtils.generateShortCode();
      attempts++;
      if (attempts > 10) {
        shortCode = urlUtils.generateShortCode(8);
      }
    } while (urlRepository.existsByShortCode(shortCode) && attempts < 20);

    if (urlRepository.existsByShortCode(shortCode)) {
      throw new RuntimeException("Unable to generate unique short code");
    }

    return shortCode;
  }
}
