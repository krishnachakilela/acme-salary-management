package com.acme.salary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CountryCodeTest {

    @Test
    void test_of_validCode_normalizesToUpperCase() {
        // Arrange + Act
        CountryCode code = CountryCode.of("de");

        // Assert
        assertEquals("DE", code.value());
    }

    @Test
    void test_of_null_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> CountryCode.of(null));
    }

    @Test
    void test_of_blank_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> CountryCode.of(" "));
    }

    @Test
    void test_of_invalidLength_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> CountryCode.of("USA"));
    }

    @Test
    void test_equals_sameNormalizedValue_areEqual() {
        // Arrange + Act + Assert
        assertEquals(CountryCode.of("us"), CountryCode.of("US"));
    }
}
 