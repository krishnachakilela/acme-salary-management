package com.acme.salary.presentation.dto;

import java.util.List;

public record AnalyticsDistributionResponse(String currencyCode, List<BandCountDto> bands) {}
