package com.acme.salary.infrastructure.persistence;

import com.acme.salary.domain.Employee;
import com.acme.salary.domain.EmployeeStatus;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeSpecifications {
    private EmployeeSpecifications() {}

    public static Specification<Employee> withFilters(
            String name,
            String department,
            String countryCode,
            EmployeeStatus status) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (name != null && !name.isBlank()) {
                String like = "%" + name.trim().toLowerCase() + "%";
                predicates.getExpressions().add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like)));
            }
            if (department != null && !department.isBlank())
                predicates.getExpressions().add(cb.equal(root.get("department"), department.trim()));
            if (countryCode != null && !countryCode.isBlank())
                predicates.getExpressions().add(cb.equal(root.get("countryCode"), countryCode.trim().toUpperCase()));
            if (status != null)
                predicates.getExpressions().add(cb.equal(root.get("status"), status));
            return predicates;
        };
    }
}
