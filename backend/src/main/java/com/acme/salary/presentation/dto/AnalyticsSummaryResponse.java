package com.acme.salary.presentation.dto;

import java.util.List;

public record AnalyticsSummaryResponse(long totalEmployees, List<CurrencyAggregateDto> byCurrency,
                                       List<GroupAggregateDto> byCountry, List<GroupAggregateDto> byDepartment) {}
