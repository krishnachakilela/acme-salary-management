package com.acme.salary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acme.salary.domain.CountryCode;
import com.acme.salary.domain.Employee;
import com.acme.salary.domain.EmployeeStatus;
import com.acme.salary.domain.Money;
import com.acme.salary.domain.SalaryRecord;
import com.acme.salary.exception.ConflictException;
import com.acme.salary.exception.NotFoundException;
import com.acme.salary.infrastructure.persistence.EmployeeRepository;
import com.acme.salary.infrastructure.persistence.SalaryRecordRepository;
import com.acme.salary.presentation.dto.CreateEmployeeRequest;
import com.acme.salary.presentation.dto.CreateSalaryRequest;
import com.acme.salary.presentation.dto.EmployeeDetailResponse;
import com.acme.salary.presentation.dto.PageResponse;
import com.acme.salary.presentation.dto.UpdateEmployeeRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SalaryRecordRepository salaryRecordRepository;

    private EmployeeService employeeService;
    private Employee employee;
    private SalaryRecord salaryRecord;

    @BeforeEach
    void arrange() {
        employeeService = new EmployeeService(employeeRepository, salaryRecordRepository);
        employee = Employee.create(
                "EMP00000010",
                "Alex",
                "Smith",
                "alex.smith@acme.example",
                "Engineering",
                CountryCode.of("US"),
                "USD");
        salaryRecord = SalaryRecord.create(
                employee.getId(),
                new Money(5_000_000L, "USD"),
                LocalDate.of(2024, 1, 1),
                "Initial hire salary");
    }

    @Test
    void test_search_validFilters_returnsMappedPage() {
        // Arrange
        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(employee)));
        when(salaryRecordRepository.findFirstByEmployeeIdOrderByEffectiveFromDescCreatedAtDesc(employee.getId()))
                .thenReturn(Optional.of(salaryRecord));

        // Act
        PageResponse<?> page = employeeService.search("Smith", "Engineering", "US", "ACTIVE", 0, 20);

        // Assert
        assertEquals(1, page.content().size());
        assertEquals(1, page.totalElements());
    }

    @Test
    void test_search_filterTooLong_throwsIllegalArgumentException() {
        // Arrange
        String tooLong = "x".repeat(101);

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.search(tooLong, null, null, null, 0, 20));
    }

    @Test
    void test_search_invalidStatus_throwsIllegalArgumentException() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.search(null, null, null, "NOT_A_STATUS", 0, 20));
    }

    @Test
    void test_getById_missingEmployee_throwsNotFoundException() {
        // Arrange
        UUID missingId = UUID.randomUUID();
        when(employeeRepository.findById(missingId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> employeeService.getById(missingId));
    }

    @Test
    void test_create_duplicateEmployeeNumber_throwsConflictException() {
        // Arrange
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "EMP00000010",
                "Alex",
                "Smith",
                "alex.smith@acme.example",
                "Engineering",
                "US",
                "USD",
                5_000_000L,
                LocalDate.of(2024, 1, 1),
                "Initial hire salary");
        when(employeeRepository.existsByEmployeeNumber(request.employeeNumber())).thenReturn(true);

        // Act + Assert
        assertThrows(ConflictException.class, () -> employeeService.create(request));
    }

    @Test
    void test_create_duplicateEmail_throwsConflictException() {
        // Arrange
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "EMP00000010",
                "Alex",
                "Smith",
                "alex.smith@acme.example",
                "Engineering",
                "US",
                "USD",
                5_000_000L,
                LocalDate.of(2024, 1, 1),
                "Initial hire salary");
        when(employeeRepository.existsByEmployeeNumber(request.employeeNumber())).thenReturn(false);
        when(employeeRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        // Act + Assert
        assertThrows(ConflictException.class, () -> employeeService.create(request));
    }

    @Test
    void test_create_validRequest_persistsEmployeeAndSalary() {
        // Arrange
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "EMP00000010",
                "Alex",
                "Smith",
                "alex.smith@acme.example",
                "Engineering",
                "US",
                "USD",
                5_000_000L,
                LocalDate.of(2024, 1, 1),
                "Initial hire salary");
        when(employeeRepository.existsByEmployeeNumber(request.employeeNumber())).thenReturn(false);
        when(employeeRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(salaryRecordRepository.save(any(SalaryRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            Employee saved = Employee.create(
                    request.employeeNumber(),
                    request.firstName(),
                    request.lastName(),
                    request.email(),
                    request.department(),
                    CountryCode.of(request.countryCode()),
                    request.currencyCode());
            return Optional.of(new Employee(
                    id,
                    saved.getEmployeeNumber(),
                    saved.getFirstName(),
                    saved.getLastName(),
                    saved.getEmail(),
                    saved.getDepartment(),
                    CountryCode.of(saved.getCountryCode()),
                    saved.getCurrencyCode(),
                    EmployeeStatus.ACTIVE,
                    saved.getCreatedAt()));
        });
        when(salaryRecordRepository.findByEmployeeIdOrderByEffectiveFromDescCreatedAtDesc(any(UUID.class)))
                .thenReturn(List.of(salaryRecord));

        // Act
        EmployeeDetailResponse response = employeeService.create(request);

        // Assert
        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        ArgumentCaptor<SalaryRecord> salaryCaptor = ArgumentCaptor.forClass(SalaryRecord.class);
        verify(employeeRepository).save(employeeCaptor.capture());
        verify(salaryRecordRepository).save(salaryCaptor.capture());
        assertEquals("EMP00000010", employeeCaptor.getValue().getEmployeeNumber());
        assertEquals(5_000_000L, salaryCaptor.getValue().getAmountMinor());
        assertEquals("EMP00000010", response.employee().employeeNumber());
    }

    @Test
    void test_update_emailConflict_throwsConflictException() {
        // Arrange
        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                "Alex",
                "Smith",
                "taken@acme.example",
                "Engineering",
                "US",
                "USD",
                "ACTIVE");
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("taken@acme.example", employee.getId()))
                .thenReturn(true);

        // Act + Assert
        assertThrows(ConflictException.class, () -> employeeService.update(employee.getId(), request));
    }

    @Test
    void test_addSalary_missingEmployee_throwsNotFoundException() {
        // Arrange
        UUID missingId = UUID.randomUUID();
        CreateSalaryRequest request = new CreateSalaryRequest(
                6_000_000L, LocalDate.of(2025, 1, 1), "Annual raise");
        when(employeeRepository.findById(missingId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> employeeService.addSalary(missingId, request));
    }

    @Test
    void test_addSalary_inactiveEmployee_throwsConflictException() {
        // Arrange
        employee.updateDemographics(
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment(),
                CountryCode.of(employee.getCountryCode()),
                employee.getCurrencyCode(),
                EmployeeStatus.INACTIVE);
        CreateSalaryRequest request = new CreateSalaryRequest(
                6_000_000L, LocalDate.of(2025, 1, 1), "Annual raise");
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        // Act + Assert
        ConflictException ex = assertThrows(
                ConflictException.class, () -> employeeService.addSalary(employee.getId(), request));
        assertEquals("Salary changes are allowed only for active employees", ex.getMessage());
        verify(salaryRecordRepository, never()).save(any());
    }

    @Test
    void test_addSalary_duplicateEffectiveFrom_throwsConflictException() {
        // Arrange
        CreateSalaryRequest request = new CreateSalaryRequest(
                6_000_000L, LocalDate.of(2024, 1, 1), "Duplicate date");
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(salaryRecordRepository.existsByEmployeeIdAndEffectiveFrom(employee.getId(), request.effectiveFrom()))
                .thenReturn(true);

        // Act + Assert
        ConflictException ex = assertThrows(
                ConflictException.class, () -> employeeService.addSalary(employee.getId(), request));
        assertEquals("Salary already exists for this effective date", ex.getMessage());
        verify(salaryRecordRepository, never()).save(any());
    }
}
