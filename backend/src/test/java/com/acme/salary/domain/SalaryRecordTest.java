package com.acme.salary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class SalaryRecordTest {

    @Test
    void test_create_validInput_storesMoneyAndReason() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        Money money = new Money(5_000_000L, "USD");
        LocalDate effectiveFrom = LocalDate.of(2024, 1, 1);

        // Act
        SalaryRecord record = SalaryRecord.create(employeeId, money, effectiveFrom, " Initial hire ");

        // Assert
        assertEquals(employeeId, record.getEmployeeId());
        assertEquals(5_000_000L, record.getAmountMinor());
        assertEquals("USD", record.getCurrencyCode());
        assertEquals(effectiveFrom, record.getEffectiveFrom());
        assertEquals("Initial hire", record.getChangeReason());
    }

    @Test
    void test_create_blankReason_throwsIllegalArgumentException() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        Money money = new Money(5_000_000L, "USD");

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> SalaryRecord.create(employeeId, money, LocalDate.of(2024, 1, 1), "  "));
    }

    @Test
    void test_create_reasonTooLong_throwsIllegalArgumentException() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        Money money = new Money(5_000_000L, "USD");
        String tooLong = "x".repeat(256);

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> SalaryRecord.create(employeeId, money, LocalDate.of(2024, 1, 1), tooLong));
    }
}
