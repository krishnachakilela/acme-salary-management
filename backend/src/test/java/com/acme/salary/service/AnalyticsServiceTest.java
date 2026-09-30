package com.acme.salary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.acme.salary.infrastructure.persistence.EmployeeRepository;
import com.acme.salary.infrastructure.persistence.SalaryRecordRepository;
import com.acme.salary.presentation.dto.AnalyticsDistributionResponse;
import com.acme.salary.presentation.dto.AnalyticsSummaryResponse;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SalaryRecordRepository salaryRecordRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void arrange() {
        analyticsService = new AnalyticsService(employeeRepository, salaryRecordRepository);
    }

    @Test
    void test_summary_mapsAggregatesFromRepositories() {
        // Arrange
        when(employeeRepository.count()).thenReturn(2L);
        when(salaryRecordRepository.aggregateByCurrency()).thenReturn(List.of(
                currencyAgg("USD", 2L, 10_000_000L, 5_000_000.0)));
        when(salaryRecordRepository.aggregateByCountry()).thenReturn(List.of(
                groupAgg("US", 2L, 5_000_000.0)));
        when(salaryRecordRepository.aggregateByDepartment()).thenReturn(List.of(
                groupAgg("Engineering", 2L, 5_000_000.0)));

        // Act
        AnalyticsSummaryResponse summary = analyticsService.summary();

        // Assert
        assertEquals(2L, summary.totalEmployees());
        assertEquals(1, summary.byCurrency().size());
        assertEquals("USD", summary.byCurrency().getFirst().currencyCode());
        assertEquals(10_000_000L, summary.byCurrency().getFirst().totalMinor());
        assertEquals(5_000_000L, summary.byCurrency().getFirst().averageMinor());
        assertEquals("US", summary.byCountry().getFirst().key());
        assertEquals("Engineering", summary.byDepartment().getFirst().key());
    }

    @Test
    void test_distribution_blankCurrency_passesNullFilter() {
        // Arrange
        when(salaryRecordRepository.distributionByBand(null)).thenReturn(List.of(
                bandAgg("50k-80k", 3L)));

        // Act
        AnalyticsDistributionResponse response = analyticsService.distribution("  ");

        // Assert
        assertNull(response.currencyCode());
        assertEquals(1, response.bands().size());
        assertEquals("50k-80k", response.bands().getFirst().band());
        assertEquals(3L, response.bands().getFirst().headcount());
    }

    @Test
    void test_distribution_currencyCode_normalizesToUpperCase() {
        // Arrange
        when(salaryRecordRepository.distributionByBand("EUR")).thenReturn(List.of(
                bandAgg("30k-50k", 1L)));

        // Act
        AnalyticsDistributionResponse response = analyticsService.distribution("eur");

        // Assert
        assertEquals("EUR", response.currencyCode());
        assertEquals(1, response.bands().size());
    }

    private static SalaryRecordRepository.CurrencyAgg currencyAgg(
            String currencyCode, long headcount, long totalMinor, double averageMinor) {
        return new SalaryRecordRepository.CurrencyAgg() {
            @Override
            public String getCurrencyCode() {
                return currencyCode;
            }

            @Override
            public long getHeadcount() {
                return headcount;
            }

            @Override
            public long getTotalMinor() {
                return totalMinor;
            }

            @Override
            public double getAverageMinor() {
                return averageMinor;
            }
        };
    }

    private static SalaryRecordRepository.GroupAgg groupAgg(
            String groupKey, long headcount, double averageMinor) {
        return new SalaryRecordRepository.GroupAgg() {
            @Override
            public String getGroupKey() {
                return groupKey;
            }

            @Override
            public long getHeadcount() {
                return headcount;
            }

            @Override
            public double getAverageMinor() {
                return averageMinor;
            }
        };
    }

    private static SalaryRecordRepository.BandAgg bandAgg(String band, long headcount) {
        return new SalaryRecordRepository.BandAgg() {
            @Override
            public String getBand() {
                return band;
            }

            @Override
            public long getHeadcount() {
                return headcount;
            }
        };
    }
}
