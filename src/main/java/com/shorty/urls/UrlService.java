package com.shorty.urls;

import com.shorty.common.exception.ResourceNotFoundException;
import com.shorty.common.exception.ValidationException;
import com.shorty.common.util.UrlUtils;
import com.shorty.users.User;
import com.shorty.users.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UrlService {

  private final UrlRepository urlRepository;
  private final UserRepository userRepository;
  private final UrlUtils urlUtils;
  private final PasswordEncoder passwordEncoder;

  public UrlService(
      UrlRepository urlRepository,
      UserRepository userRepository,
      UrlUtils urlUtils,
      PasswordEncoder passwordEncoder) {
    this.urlRepository = urlRepository;
    this.userRepository = userRepository;
    this.urlUtils = urlUtils;
    this.passwordEncoder = passwordEncoder;
  }

  public Url createShortUrl(
      String originalUrl,
      String customCode,
      UrlVisibility visibility,
      LocalDateTime expiresAt,
      Integer clickLimit,
      String description,
      String password,
      User user) {
    if (!urlUtils.isValidUrl(originalUrl)) {
      throw new ValidationException("Invalid URL format");
    }

    String normalizedUrl = urlUtils.normalizeUrl(originalUrl);

    String shortCode =
        customCode != null && !customCode.trim().isEmpty()
            ? validateAndGetCustomCode(customCode.trim())
            : generateUniqueShortCode();

    Url url = new Url(normalizedUrl, shortCode, user);
    url.setVisibility(visibility != null ? visibility : UrlVisibility.PUBLIC);
    url.setExpiresAt(expiresAt);
    url.setDescription(description);

    if (clickLimit != null && clickLimit > 0) {
      url.setClickLimit(clickLimit);
    }

    if (password != null && !password.trim().isEmpty()) {
      url.setPasswordProtected(true);
      url.setPasswordHash(passwordEncoder.encode(password));
    }

    return urlRepository.save(url);
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

  public Url updateUrl(
      UUID urlId,
      UUID userId,
      String originalUrl,
      UrlVisibility visibility,
      LocalDateTime expiresAt,
      Integer clickLimit,
      String description,
      String password,
      Boolean removePassword) {
    Url url =
        urlRepository
            .findById(urlId)
            .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + urlId));

    if (url.isOwnedBy(findUserById(userId))) {
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

    url.setExpiresAt(expiresAt);
    url.setDescription(description);

    if (clickLimit != null) {
      url.setClickLimit(clickLimit);
    }

    if (removePassword != null && removePassword) {
      url.setPasswordProtected(false);
      url.setPasswordHash(null);
    } else if (password != null && !password.trim().isEmpty()) {
      url.setPasswordProtected(true);
      url.setPasswordHash(passwordEncoder.encode(password));
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

  public boolean verifyUrlPassword(String shortCode, String password) {
    Optional<Url> urlOpt = urlRepository.findByShortCodeAndActiveTrue(shortCode);
    if (urlOpt.isEmpty()) {
      return false;
    }

    Url url = urlOpt.get();
    if (!url.isPasswordProtected()) {
      return true;
    }

    return password != null && passwordEncoder.matches(password, url.getPasswordHash());
  }

  @Transactional(readOnly = true)
  public List<Url> getUrlsExpiringSoon(UUID userId, int hours) {
    LocalDateTime threshold = LocalDateTime.now().plusHours(hours);
    return urlRepository.findUrlsExpiringBefore(userId, threshold);
  }

  public int cleanupExpiredUrls() {
    LocalDateTime now = LocalDateTime.now();
    List<Url> expiredUrls = urlRepository.findExpiredUrls(now);

    expiredUrls.forEach(url -> url.setActive(false));
    urlRepository.saveAll(expiredUrls);

    return expiredUrls.size();
  }

  public Url resetClickCount(UUID urlId, UUID userId) {
    Url url =
        urlRepository
            .findById(urlId)
            .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + urlId));

    if (url.isOwnedBy(findUserById(userId))) {
      throw new ValidationException("You don't have permission to reset this URL's click count");
    }

    url.setClickCount(0);
    return urlRepository.save(url);
  }

  public void logClickCount(UUID urlId) {
    Url url =
        urlRepository
            .findById(urlId)
            .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + urlId));

    url.incrementClickCount();
    urlRepository.save(url);
  }

  public Url extendExpiration(UUID urlId, UUID userId, LocalDateTime newExpirationDate) {
    Url url =
        urlRepository
            .findById(urlId)
            .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + urlId));

    if (url.isOwnedBy(findUserById(userId))) {
      throw new ValidationException("You don't have permission to extend this URL's expiration");
    }

    if (newExpirationDate != null && newExpirationDate.isBefore(LocalDateTime.now())) {
      throw new ValidationException("New expiration date must be in the future");
    }

    url.setExpiresAt(newExpirationDate);
    return urlRepository.save(url);
  }

  private String validateAndGetCustomCode(String customCode) {
    if (customCode.length() < 3) {
      throw new ValidationException("Custom code must be at least 3 characters long");
    }

    if (urlRepository.existsByShortCode(customCode)) {
      throw new ValidationException("Custom code already exists: " + customCode);
    }

    return customCode;
  }

  private User findUserById(UUID userId) {
    return userRepository.findById(userId).get();
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
