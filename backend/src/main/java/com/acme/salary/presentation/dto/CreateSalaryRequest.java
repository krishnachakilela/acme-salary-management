package com.acme.salary.presentation.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateSalaryRequest(@NotNull @Min(1) @Max(100_000_000_000L) Long amountMinor,
                                  @NotNull LocalDate effectiveFrom,
                                  @NotBlank @Size(min = 1, max = 255) String changeReason) {}
