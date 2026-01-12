package com.shorty.utils;

import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShortCodeGenerator {

    private static final String BASE62_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Value("${app.shortcode.length:7}")
    private int shortCodeLength;

    private final SecureRandom secureRandom;

    public ShortCodeGenerator() {
        this.secureRandom = new SecureRandom();
    }

    public String generate() {
        return generate(shortCodeLength);
    }

    public String generate(int length) {
        if (length < 3 || length > 10) {
            throw new IllegalArgumentException("Code length must be between 3 and 10");
        }

        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(BASE62_ALPHABET.length());
            code.append(BASE62_ALPHABET.charAt(randomIndex));
        }

        return code.toString();
    }

    public boolean isValidAlias(String alias) {
        if (alias == null || alias.isBlank()) {
            return false;
        }

        if (alias.length() < 3 || alias.length() > 10) {
            return false;
        }

        return alias.matches("^[a-zA-Z0-9]+$");
    }
}
