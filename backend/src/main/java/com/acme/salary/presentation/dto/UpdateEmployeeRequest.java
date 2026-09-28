package com.acme.salary.presentation.dto;

import jakarta.validation.constraints.*;

public record UpdateEmployeeRequest(
        @NotBlank @Size(min = 1, max = 100) String firstName, @NotBlank @Size(min = 1, max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email, @NotBlank @Size(min = 1, max = 100) String department,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String countryCode,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currencyCode,
        @NotBlank @Pattern(regexp = "^(ACTIVE|INACTIVE)$") String status) {}
