package com.acme.salary.presentation.controller;

import static com.acme.salary.infrastructure.config.OpenApiConfig.BEARER_AUTH_SCHEME;

import com.acme.salary.service.AnalyticsService;
import com.acme.salary.presentation.dto.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {this.analyticsService = analyticsService;}

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> summary() {return ResponseEntity.ok(analyticsService.summary());}

    @GetMapping("/distribution")
    public ResponseEntity<AnalyticsDistributionResponse> distribution(@RequestParam(required = false) String currencyCode) {
        return ResponseEntity.ok(analyticsService.distribution(currencyCode));
    }
}
