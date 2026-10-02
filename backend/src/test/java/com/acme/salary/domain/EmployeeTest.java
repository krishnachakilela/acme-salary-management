package com.acme.salary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmployeeTest {

    @Test
    void test_create_validInput_setsActiveStatusAndNormalizedEmail() {
        // Arrange + Act
        Employee employee = Employee.create(
                "EMP00000010",
                " Alex ",
                " Smith ",
                "Alex.Smith@Acme.Example",
                " Engineering ",
                CountryCode.of("us"),
                "usd");

        // Assert
        assertEquals("EMP00000010", employee.getEmployeeNumber());
        assertEquals("Alex", employee.getFirstName());
        assertEquals("Smith", employee.getLastName());
        assertEquals("alex.smith@acme.example", employee.getEmail());
        assertEquals("Engineering", employee.getDepartment());
        assertEquals("US", employee.getCountryCode());
        assertEquals("USD", employee.getCurrencyCode());
        assertEquals(EmployeeStatus.ACTIVE, employee.getStatus());
    }

    @Test
    void test_create_invalidEmployeeNumber_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> Employee.create(
                "BAD-001",
                "Alex",
                "Smith",
                "alex.smith@acme.example",
                "Engineering",
                CountryCode.of("US"),
                "USD"));
    }

    @Test
    void test_create_invalidEmail_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> Employee.create(
                "EMP00000010",
                "Alex",
                "Smith",
                "not-an-email",
                "Engineering",
                CountryCode.of("US"),
                "USD"));
    }

    @Test
    void test_updateDemographics_validInput_updatesFieldsAndStatus() {
        // Arrange
        Employee employee = Employee.create(
                "EMP00000010",
                "Alex",
                "Smith",
                "alex.smith@acme.example",
                "Engineering",
                CountryCode.of("US"),
                "USD");

        // Act
        employee.updateDemographics(
                "Jordan",
                "Lee",
                "jordan.lee@acme.example",
                "Sales",
                CountryCode.of("DE"),
                "EUR",
                EmployeeStatus.INACTIVE);

        // Assert
        assertEquals("Jordan", employee.getFirstName());
        assertEquals("Lee", employee.getLastName());
        assertEquals("jordan.lee@acme.example", employee.getEmail());
        assertEquals("Sales", employee.getDepartment());
        assertEquals("DE", employee.getCountryCode());
        assertEquals("EUR", employee.getCurrencyCode());
        assertEquals(EmployeeStatus.INACTIVE, employee.getStatus());
    }

    @Test
    void test_create_blankFirstName_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> Employee.create(
                "EMP00000010",
                "  ",
                "Smith",
                "alex.smith@acme.example",
                "Engineering",
                CountryCode.of("US"),
                "USD"));
    }

    @Test
    void test_isEligibleForSalaryChange_active_returnsTrue() {
        // Arrange
        Employee employee = Employee.create(
                "EMP00000010",
                "Alex",
                "Smith",
                "alex.smith@acme.example",
                "Engineering",
                CountryCode.of("US"),
                "USD");

        // Act + Assert
        assertTrue(employee.isEligibleForSalaryChange());
    }

    @Test
    void test_isEligibleForSalaryChange_inactive_returnsFalse() {
        // Arrange
        Employee employee = Employee.create(
                "EMP00000010",
                "Alex",
                "Smith",
                "alex.smith@acme.example",
                "Engineering",
                CountryCode.of("US"),
                "USD");
        employee.updateDemographics(
                "Alex",
                "Smith",
                "alex.smith@acme.example",
                "Engineering",
                CountryCode.of("US"),
                "USD",
                EmployeeStatus.INACTIVE);

        // Act + Assert
        assertFalse(employee.isEligibleForSalaryChange());
    }
}
