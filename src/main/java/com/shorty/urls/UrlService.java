package com.shorty.urls;

import com.shorty.common.util.UrlUtils;
import com.shorty.common.util.exception.ResourceNotFoundException;
import com.shorty.common.util.exception.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

  private final Map<String, Url> urlsByShortCode = new ConcurrentHashMap<>();
  private final Map<String, Url> urlsById = new ConcurrentHashMap<>();
  private final UrlUtils urlUtils;

  public UrlService(UrlUtils urlUtils) {
    this.urlUtils = urlUtils;
  }

  public Url createShortUrl(String originalUrl, String customCode) {
    if (!urlUtils.isValidUrl(originalUrl)) {
      throw new ValidationException("Invalid URL format");
    }

    String normalizedUrl = urlUtils.normalizeUrl(originalUrl);

    String shortCode =
        customCode != null && !customCode.trim().isEmpty()
            ? customCode.trim()
            : generateUniqueShortCode();

    if (urlsByShortCode.containsKey(shortCode)) {
      throw new ValidationException("Short code already exists: " + shortCode);
    }

    Url url = Url.create(normalizedUrl, shortCode);
    urlsByShortCode.put(shortCode, url);
    urlsById.put(url.id(), url);

    return url;
  }

  public Optional<Url> findByShortCode(String shortCode) {
    Url url = urlsByShortCode.get(shortCode);
    return url != null && url.isActive() ? Optional.of(url) : Optional.empty();
  }

  public List<Url> getAllUrls() {
    return List.copyOf(urlsById.values());
  }

  public Url updateUrlStatus(String id, boolean active) {
    Url existingUrl = urlsById.get(id);
    if (existingUrl == null) {
      throw new ResourceNotFoundException("URL not found with id: " + id);
    }

    Url updatedUrl = existingUrl.withActiveStatus(active);
    urlsById.put(id, updatedUrl);
    urlsByShortCode.put(updatedUrl.shortCode(), updatedUrl);

    return updatedUrl;
  }

  public long getUrlCount() {
    return urlsById.size();
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
    } while (urlsByShortCode.containsKey(shortCode) && attempts < 20);

    if (urlsByShortCode.containsKey(shortCode)) {
      throw new RuntimeException("Unable to generate unique short code");
    }

    return shortCode;
  }
}
