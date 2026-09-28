package com.acme.salary.presentation.dto;

import java.util.List;

public record EmployeeDetailResponse(EmployeeResponse employee, List<SalaryResponse> salaryHistory) {}
