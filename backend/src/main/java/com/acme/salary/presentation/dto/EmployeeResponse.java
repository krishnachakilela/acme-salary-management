package com.acme.salary.presentation.dto;

import java.util.UUID;

public record EmployeeResponse(UUID id, String employeeNumber, String firstName, String lastName, String email,
                               String department, String countryCode, String currencyCode, String status,
                               SalaryResponse currentSalary) {}
