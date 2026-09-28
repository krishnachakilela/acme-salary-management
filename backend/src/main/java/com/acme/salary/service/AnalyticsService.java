package com.acme.salary.service;

import com.acme.salary.infrastructure.persistence.*;
import com.acme.salary.presentation.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
    private final EmployeeRepository employeeRepository;
    private final SalaryRecordRepository salaryRecordRepository;

    public AnalyticsService(EmployeeRepository employeeRepository, SalaryRecordRepository salaryRecordRepository) {
        this.employeeRepository = employeeRepository;
        this.salaryRecordRepository = salaryRecordRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse summary() {
        return new AnalyticsSummaryResponse(employeeRepository.count(),
                salaryRecordRepository.aggregateByCurrency().stream().map(
                        p -> new CurrencyAggregateDto(p.getCurrencyCode(), p.getHeadcount(), p.getTotalMinor(),
                                Math.round(p.getAverageMinor()))).toList(),
                salaryRecordRepository.aggregateByCountry().stream()
                        .map(p -> new GroupAggregateDto(p.getGroupKey(), p.getHeadcount(), Math.round(p.getAverageMinor())))
                        .toList(),
                salaryRecordRepository.aggregateByDepartment().stream()
                        .map(p -> new GroupAggregateDto(p.getGroupKey(), p.getHeadcount(), Math.round(p.getAverageMinor())))
                        .toList());
    }

    @Transactional(readOnly = true)
    public AnalyticsDistributionResponse distribution(String currencyCode) {
        String normalized = (currencyCode == null || currencyCode.isBlank()) ? null : currencyCode.trim().toUpperCase();
        return new AnalyticsDistributionResponse(normalized,
                salaryRecordRepository.distributionByBand(normalized).stream()
                        .map(p -> new BandCountDto(p.getBand(), p.getHeadcount())).toList());
    }
}
