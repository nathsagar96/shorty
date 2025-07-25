package com.shorty.common.validation;

import java.net.IDN;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class UrlValidationService {

  private static final Set<String> BLOCKED_PROTOCOLS = Set.of("javascript", "data", "file", "ftp");
  private static final Set<String> BLOCKED_DOMAINS =
      Set.of("localhost", "127.0.0.1", "0.0.0.0", "::1");
  private static final Pattern IP_PATTERN =
      Pattern.compile(
          "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
  private static final Pattern SUSPICIOUS_PATTERN =
      Pattern.compile(
          ".*(?:bit\\.ly|tinyurl|t\\.co|short\\.link|ow\\.ly)/.*", Pattern.CASE_INSENSITIVE);

  public ValidationResult validateUrl(String url) {
    ValidationResult result = new ValidationResult();

    if (url == null || url.trim().isEmpty()) {
      result.addError("URL cannot be null or empty");
      return result;
    }

    try {
      java.net.URL parsedUrl = new java.net.URL(url);

      validateProtocol(parsedUrl, result);
      validateDomain(parsedUrl, result);
      validateLength(url, result);
      validateSecurity(url, result);
      validateFormat(url, result);

    } catch (Exception e) {
      result.addError("Invalid URL format: " + e.getMessage());
    }

    return result;
  }

  public ValidationResult validateCustomCode(String customCode) {
    ValidationResult result = new ValidationResult();

    if (customCode == null || customCode.trim().isEmpty()) {
      return result;
    }

    String trimmed = customCode.trim();

    if (trimmed.length() < 3) {
      result.addError("Custom code must be at least 3 characters long");
    }

    if (trimmed.length() > 50) {
      result.addError("Custom code must be at most 50 characters long");
    }

    if (!trimmed.matches("^[a-zA-Z0-9-_]+$")) {
      result.addError("Custom code can only contain letters, numbers, hyphens, and underscores");
    }

    if (isReservedWord(trimmed)) {
      result.addError("Custom code uses a reserved word");
    }

    return result;
  }

  public ValidationResult validateExpirationDate(LocalDateTime expiresAt) {
    ValidationResult result = new ValidationResult();

    if (expiresAt == null) {
      return result;
    }

    if (expiresAt.isBefore(LocalDateTime.now())) {
      result.addError("Expiration date must be in the future");
    }

    if (expiresAt.isAfter(LocalDateTime.now().plusYears(10))) {
      result.addError("Expiration date cannot be more than 10 years in the future");
    }

    return result;
  }

  private void validateProtocol(java.net.URL url, ValidationResult result) {
    String protocol = url.getProtocol().toLowerCase();

    if (BLOCKED_PROTOCOLS.contains(protocol)) {
      result.addError("Protocol '" + protocol + "' is not allowed");
    }

    if (!protocol.equals("http") && !protocol.equals("https")) {
      result.addWarning("Only HTTP and HTTPS protocols are recommended");
    }

    if (protocol.equals("http")) {
      result.addWarning("HTTP URLs are less secure than HTTPS");
    }
  }

  private void validateDomain(java.net.URL url, ValidationResult result) {
    String host = url.getHost().toLowerCase();

    if (BLOCKED_DOMAINS.contains(host)) {
      result.addError("Domain '" + host + "' is not allowed");
    }

    if (IP_PATTERN.matcher(host).matches()) {
      result.addWarning("IP addresses are discouraged, use domain names instead");
    }

    try {
      String asciiHost = IDN.toASCII(host);
      if (!asciiHost.equals(host)) {
        result.addInfo("International domain detected: " + asciiHost);
      }
    } catch (Exception e) {
      result.addError("Invalid international domain name");
    }

    if (host.length() > 253) {
      result.addError("Domain name too long");
    }
  }

  private void validateLength(String url, ValidationResult result) {
    if (url.length() > 2048) {
      result.addError("URL too long (maximum 2048 characters)");
    }

    if (url.length() > 1000) {
      result.addWarning("Very long URL detected, consider shortening");
    }
  }

  private void validateSecurity(String url, ValidationResult result) {
    if (SUSPICIOUS_PATTERN.matcher(url).matches()) {
      result.addWarning("URL appears to be already shortened");
    }

    if (url.contains("<") || url.contains(">") || url.contains("\"")) {
      result.addError("URL contains potentially dangerous characters");
    }

    if (url.contains("redirect") || url.contains("redir")) {
      result.addWarning("URL may contain redirects");
    }
  }

  private void validateFormat(String url, ValidationResult result) {
    if (url.contains(" ")) {
      result.addError("URL cannot contain spaces");
    }

    if (!url.matches("^https?://[^\\s]+$")) {
      result.addError("URL format is invalid");
    }
  }

  private boolean isReservedWord(String word) {
    Set<String> reserved =
        Set.of(
            "api",
            "admin",
            "www",
            "mail",
            "ftp",
            "localhost",
            "dashboard",
            "login",
            "register",
            "signup",
            "signin",
            "auth",
            "oauth",
            "health",
            "metrics",
            "actuator",
            "static",
            "assets",
            "public");
    return reserved.contains(word.toLowerCase());
  }

  public static class ValidationResult {
    private final List<String> errors = new java.util.ArrayList<>();
    private final List<String> warnings = new java.util.ArrayList<>();
    private final List<String> info = new java.util.ArrayList<>();

    public void addError(String error) {
      errors.add(error);
    }

    public void addWarning(String warning) {
      warnings.add(warning);
    }

    public void addInfo(String info) {
      this.info.add(info);
    }

    public boolean isValid() {
      return errors.isEmpty();
    }

    public List<String> getErrors() {
      return List.copyOf(errors);
    }

    public List<String> getWarnings() {
      return List.copyOf(warnings);
    }

    public List<String> getInfo() {
      return List.copyOf(info);
    }

    public String getErrorMessage() {
      return String.join("; ", errors);
    }
  }
}
