package com.shorty.utils;

import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShortCodeGenerator {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.shortcode.length:7}")
    private int shortCodeLength;

    public String generate() {
        StringBuilder shortCode = new StringBuilder(shortCodeLength);

        for (int i = 0; i < shortCodeLength; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            shortCode.append(CHARACTERS.charAt(index));
        }

        return shortCode.toString();
    }
}
