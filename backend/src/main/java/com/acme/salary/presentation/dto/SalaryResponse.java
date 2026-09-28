package com.acme.salary.presentation.dto;

import java.time.LocalDate;
import java.util.UUID;

public record SalaryResponse(UUID id, long amountMinor, String currencyCode, LocalDate effectiveFrom,
                             String changeReason) {}
