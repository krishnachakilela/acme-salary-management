package com.acme.salary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void test_create_validAmount_returnsNormalizedCurrency() {
        // Arrange + Act
        Money money = new Money(10_000L, "usd");

        // Assert
        assertEquals(10_000L, money.amountMinor());
        assertEquals("USD", money.currencyCode());
    }

    @Test
    void test_create_zeroAmount_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new Money(0L, "USD"));
    }

    @Test
    void test_create_negativeAmount_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new Money(-1L, "USD"));
    }

    @Test
    void test_create_amountAboveMax_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new Money(100_000_000_001L, "USD"));
    }

    @Test
    void test_create_invalidCurrency_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new Money(100L, "US"));
    }

    @Test
    void test_create_blankCurrency_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new Money(100L, "  "));
    }

    @Test
    void test_equals_sameValues_areEqual() {
        // Arrange
        Money left = new Money(500L, "EUR");
        Money right = new Money(500L, "eur");

        // Act + Assert
        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
    }

    @Test
    void test_equals_differentAmount_areNotEqual() {
        // Arrange + Act + Assert
        assertNotEquals(new Money(500L, "EUR"), new Money(501L, "EUR"));
    }
}
