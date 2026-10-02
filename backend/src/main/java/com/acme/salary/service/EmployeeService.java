package com.acme.salary.service;

import com.acme.salary.domain.*;
import com.acme.salary.exception.ConflictException;
import com.acme.salary.exception.NotFoundException;
import com.acme.salary.infrastructure.persistence.*;
import com.acme.salary.presentation.dto.*;

import java.util.*;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {
    private static final int MAX_PAGE = 100, DEFAULT_PAGE = 20, MAX_FILTER_LENGTH = 100;
    private static final Sort LIST_SORT = Sort.by(Sort.Order.asc("employeeNumber"));
    private final EmployeeRepository employeeRepository;
    private final SalaryRecordRepository salaryRecordRepository;

    public EmployeeService(EmployeeRepository employeeRepository, SalaryRecordRepository salaryRecordRepository) {
        this.employeeRepository = employeeRepository;
        this.salaryRecordRepository = salaryRecordRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> search(
            String name,
            String department,
            String countryCode,
            String status,
            int page,
            int size) {
        int safeSize = size <= 0 ? DEFAULT_PAGE : Math.min(size, MAX_PAGE);
        int safePage = Math.max(page, 0);
        String safeName = boundedFilter(name);
        String safeDepartment = boundedFilter(department);
        String safeCountry = boundedFilter(countryCode);
        EmployeeStatus st = (status == null || status.isBlank())
                ? null
                : EmployeeStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        PageRequest pageable = PageRequest.of(safePage, safeSize, LIST_SORT);
        Page<Employee> result = employeeRepository.findAll(
                EmployeeSpecifications.withFilters(safeName, safeDepartment, safeCountry, st), pageable);
        Map<UUID, SalaryRecord> current = new HashMap<>();
        for (Employee e : result.getContent()) {
            salaryRecordRepository.findFirstByEmployeeIdOrderByEffectiveFromDesc(e.getId())
                    .ifPresent(r -> current.put(e.getId(), r));
        }
        List<EmployeeResponse> content = result.getContent().stream().map(e -> toResp(e, current.get(e.getId()))).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(), result.getTotalElements(),
                result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public EmployeeDetailResponse getById(UUID id) {
        Employee e = require(id);
        List<SalaryRecord> history = salaryRecordRepository.findByEmployeeIdOrderByEffectiveFromDesc(id);
        return new EmployeeDetailResponse(toResp(e, history.isEmpty() ? null : history.getFirst()),
                history.stream().map(EmployeeService::toSalary).toList());
    }

    @Transactional
    public EmployeeDetailResponse create(CreateEmployeeRequest req) {
        if (employeeRepository.existsByEmployeeNumber(req.employeeNumber()))
            throw new ConflictException("Employee number exists");
        if (employeeRepository.existsByEmailIgnoreCase(req.email()))
            throw new ConflictException("Email exists");
        Employee e = Employee.create(req.employeeNumber(), req.firstName(), req.lastName(), req.email(), req.department(),
                CountryCode.of(req.countryCode()), req.currencyCode());
        employeeRepository.save(e);
        salaryRecordRepository.save(
                SalaryRecord.create(e.getId(), new Money(req.initialSalaryMinor(), req.currencyCode()), req.effectiveFrom(),
                        req.changeReason()));
        return getById(e.getId());
    }

    @Transactional
    public EmployeeDetailResponse update(UUID id, UpdateEmployeeRequest req) {
        Employee e = require(id);
        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(req.email().trim().toLowerCase(Locale.ROOT), id))
            throw new ConflictException("Email exists");
        e.updateDemographics(req.firstName(), req.lastName(), req.email(), req.department(),
                CountryCode.of(req.countryCode()), req.currencyCode(),
                EmployeeStatus.valueOf(req.status().toUpperCase(Locale.ROOT)));
        employeeRepository.save(e);
        return getById(id);
    }

    @Transactional
    public EmployeeDetailResponse addSalary(UUID id, CreateSalaryRequest req) {
        Employee e = require(id);
        if (!e.isEligibleForSalaryChange()) {
            throw new ConflictException("Salary changes are allowed only for active employees");
        }
        salaryRecordRepository.save(
                SalaryRecord.create(id, new Money(req.amountMinor(), e.getCurrencyCode()), req.effectiveFrom(),
                        req.changeReason()));
        return getById(id);
    }

    private static String boundedFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_FILTER_LENGTH) {
            throw new IllegalArgumentException("Filter too long");
        }
        return trimmed;
    }

    private Employee require(UUID id) {
        return employeeRepository.findById(id).orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    private static EmployeeResponse toResp(Employee e, SalaryRecord cur) {
        return new EmployeeResponse(e.getId(), e.getEmployeeNumber(), e.getFirstName(), e.getLastName(), e.getEmail(),
                e.getDepartment(), e.getCountryCode(), e.getCurrencyCode(), e.getStatus().name(),
                cur == null ? null : toSalary(cur));
    }

    private static SalaryResponse toSalary(SalaryRecord r) {
        return new SalaryResponse(r.getId(), r.getAmountMinor(), r.getCurrencyCode(), r.getEffectiveFrom(),
                r.getChangeReason());
    }
}
