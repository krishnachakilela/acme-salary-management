package com.acme.salary.presentation.controller;

import com.acme.salary.service.EmployeeService;
import com.acme.salary.presentation.dto.*;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {this.employeeService = employeeService;}

    @GetMapping
    public ResponseEntity<PageResponse<EmployeeResponse>> list(
            @RequestParam(required = false) String name, @RequestParam(required = false) String department,
            @RequestParam(required = false) String countryCode, @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(employeeService.search(name, department, countryCode, status, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDetailResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeDetailResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @PostMapping("/{id}/salaries")
    public ResponseEntity<EmployeeDetailResponse> addSalary(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSalaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.addSalary(id, request));
    }
}
