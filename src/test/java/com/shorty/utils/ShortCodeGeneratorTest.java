package com.shorty.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShortCodeGeneratorTest {

    private ShortCodeGenerator shortCodeGenerator;

    @BeforeEach
    void setUp() {
        shortCodeGenerator = new ShortCodeGenerator();

        try {
            Field shortCodeLengthField = ShortCodeGenerator.class.getDeclaredField("shortCodeLength");
            shortCodeLengthField.setAccessible(true);
            shortCodeLengthField.set(shortCodeGenerator, 7);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set shortCodeLength field", e);
        }
    }

    @Nested
    @DisplayName("Generate Tests")
    class GenerateTests {

        @Test
        @DisplayName("Should generate short code with default length")
        void shouldGenerateShortCodeWithDefaultLength() {
            // When
            String code = shortCodeGenerator.generate();

            // Then
            assertNotNull(code);
            assertEquals(7, code.length());
            assertTrue(code.matches("^[a-zA-Z0-9]+$"));
        }

        @Test
        @DisplayName("Should generate short code with specified valid length")
        void shouldGenerateShortCodeWithSpecifiedValidLength() {
            // When
            String code = shortCodeGenerator.generate(5);

            // Then
            assertNotNull(code);
            assertEquals(5, code.length());
            assertTrue(code.matches("^[a-zA-Z0-9]+$"));
        }

        @Test
        @DisplayName("Should generate different codes on multiple calls")
        void shouldGenerateDifferentCodesOnMultipleCalls() {
            // When
            String code1 = shortCodeGenerator.generate();
            String code2 = shortCodeGenerator.generate();

            // Then
            assertNotEquals(code1, code2);
        }
    }

    @Nested
    @DisplayName("Generate with Length Validation Tests")
    class GenerateWithLengthValidationTests {

        @Test
        @DisplayName("Should throw exception when length is less than 3")
        void shouldThrowExceptionWhenLengthIsLessThan3() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> shortCodeGenerator.generate(2));
        }

        @Test
        @DisplayName("Should throw exception when length is greater than 10")
        void shouldThrowExceptionWhenLengthIsGreaterThan10() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> shortCodeGenerator.generate(11));
        }

        @Test
        @DisplayName("Should generate code when length is exactly 3")
        void shouldGenerateCodeWhenLengthIsExactly3() {
            // When
            String code = shortCodeGenerator.generate(3);

            // Then
            assertNotNull(code);
            assertEquals(3, code.length());
            assertTrue(code.matches("^[a-zA-Z0-9]+$"));
        }

        @Test
        @DisplayName("Should generate code when length is exactly 10")
        void shouldGenerateCodeWhenLengthIsExactly10() {
            // When
            String code = shortCodeGenerator.generate(10);

            // Then
            assertNotNull(code);
            assertEquals(10, code.length());
            assertTrue(code.matches("^[a-zA-Z0-9]+$"));
        }
    }

    @Nested
    @DisplayName("Is Valid Alias Tests")
    class IsValidAliasTests {

        @Test
        @DisplayName("Should return true for valid alias with letters and numbers")
        void shouldReturnTrueForValidAliasWithLettersAndNumbers() {
            // When
            boolean isValid = shortCodeGenerator.isValidAlias("abc123");

            // Then
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should return false for alias with underscores")
        void shouldReturnFalseForAliasWithUnderscores() {
            // When
            boolean isValid = shortCodeGenerator.isValidAlias("my_alias");

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for alias with hyphens")
        void shouldReturnFalseForAliasWithHyphens() {
            // When
            boolean isValid = shortCodeGenerator.isValidAlias("my-alias");

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return true for valid alias with mixed alphanumeric characters")
        void shouldReturnTrueForValidAliasWithMixedAlphanumericCharacters() {
            // When
            boolean isValid = shortCodeGenerator.isValidAlias("MyAlias123");

            // Then
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should return false for null alias")
        void shouldReturnFalseForNullAlias() {
            // When
            boolean isValid = shortCodeGenerator.isValidAlias(null);

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for blank alias")
        void shouldReturnFalseForBlankAlias() {
            // When
            boolean isValid = shortCodeGenerator.isValidAlias("   ");

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for alias shorter than 3 characters")
        void shouldReturnFalseForAliasShorterThan3Characters() {
            // When
            boolean isValid = shortCodeGenerator.isValidAlias("ab");

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for alias longer than 10 characters")
        void shouldReturnFalseForAliasLongerThan10Characters() {
            // When
            boolean isValid = shortCodeGenerator.isValidAlias("toolongalias");

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for alias with special characters")
        void shouldReturnFalseForAliasWithSpecialCharacters() {
            // When
            boolean isValid = shortCodeGenerator.isValidAlias("invalid@alias");

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for alias with spaces")
        void shouldReturnFalseForAliasWithSpaces() {
            // When
            boolean isValid = shortCodeGenerator.isValidAlias("invalid alias");

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should return false for alias with dots")
        void shouldReturnFalseForAliasWithDots() {
            // When
            boolean isValid = shortCodeGenerator.isValidAlias("invalid.alias");

            // Then
            assertFalse(isValid);
        }
    }
}
