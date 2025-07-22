package com.shorty.common.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class UrlUtils {

  private static final String CHARACTERS =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final int DEFAULT_LENGTH = 6;
  private final SecureRandom random = new SecureRandom();

  public String generateShortCode() {
    return generateShortCode(DEFAULT_LENGTH);
  }

  public String generateShortCode(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    }
    return sb.toString();
  }

  public boolean isValidUrl(String url) {
    if (url == null || url.trim().isEmpty()) {
      return false;
    }

    try {
      new java.net.URL(url);
      return url.startsWith("http://") || url.startsWith("https://");
    } catch (Exception e) {
      return false;
    }
  }

  public String normalizeUrl(String url) {
    if (url == null) return null;

    try {
      java.net.URL parsed = new java.net.URL(url.trim());
      String normalized =
          parsed.getProtocol().toLowerCase() + "://" + parsed.getHost().toLowerCase();

      if (parsed.getPort() != -1) {
        normalized += ":" + parsed.getPort();
      }

      if (parsed.getPath() != null && !parsed.getPath().isEmpty()) {
        normalized += parsed.getPath();
      }

      if (parsed.getQuery() != null) {
        normalized += "?" + parsed.getQuery();
      }

      return normalized;
    } catch (Exception e) {
      return url;
    }
  }
}
