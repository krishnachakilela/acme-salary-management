package com.acme.salary.infrastructure.persistence;

import com.acme.salary.domain.Employee;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmployeeNumber(String employeeNumber);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
}
