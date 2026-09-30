package com.acme.salary.infrastructure.persistence;

import com.acme.salary.domain.Employee;
import com.acme.salary.domain.EmployeeStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeSpecifications {
    private static final char LIKE_ESCAPE = '\\';

    private EmployeeSpecifications() {}

    public static Specification<Employee> withFilters(
            String name,
            String department,
            String countryCode,
            EmployeeStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(name)) {
                String like = containsPattern(name);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like, LIKE_ESCAPE),
                        cb.like(cb.lower(root.get("lastName")), like, LIKE_ESCAPE),
                        cb.like(cb.lower(cb.concat(cb.concat(root.get("lastName"), ", "), root.get("firstName"))), like, LIKE_ESCAPE),
                        cb.like(cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))), like, LIKE_ESCAPE)));
            }
            if (hasText(department)) {
                predicates.add(cb.equal(cb.lower(root.get("department")), department.trim().toLowerCase(Locale.ROOT)));
            }
            if (hasText(countryCode)) {
                predicates.add(cb.equal(cb.upper(root.get("countryCode")), countryCode.trim().toUpperCase(Locale.ROOT)));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String containsPattern(String raw) {
        String escaped = raw.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}