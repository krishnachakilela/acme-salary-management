package com.acme.salary.presentation.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateEmployeeRequest(
        @NotBlank @Pattern(regexp = "^EMP[0-9]{4,10}$") String employeeNumber,
        @NotBlank @Size(min = 1, max = 100) String firstName, @NotBlank @Size(min = 1, max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email, @NotBlank @Size(min = 1, max = 100) String department,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String countryCode,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currencyCode,
        @NotNull @Min(1) @Max(100_000_000_000L) Long initialSalaryMinor,
        @NotNull LocalDate effectiveFrom, @NotBlank @Size(min = 1, max = 255) String changeReason) {}
