package com.acme.salary.infrastructure.persistence;

import com.acme.salary.domain.SalaryRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, UUID> {
    // Tie-break when duplicates existed historically: newest created_at, then highest id.
    // Unique (employee_id, effective_from) makes effective_from alone sufficient going forward.
    String CURRENT_SALARY = """
      (
        SELECT employee_id, amount_minor, currency_code, effective_from
        FROM (
          SELECT employee_id, amount_minor, currency_code, effective_from, created_at, id,
                 ROW_NUMBER() OVER (
                   PARTITION BY employee_id
                   ORDER BY effective_from DESC, created_at DESC, id DESC
                 ) AS rn
          FROM salary_record
        ) ranked
        WHERE rn = 1
      )
      """;

    List<SalaryRecord> findByEmployeeIdOrderByEffectiveFromDescCreatedAtDesc(UUID employeeId);

    Optional<SalaryRecord> findFirstByEmployeeIdOrderByEffectiveFromDescCreatedAtDesc(UUID employeeId);

    boolean existsByEmployeeIdAndEffectiveFrom(UUID employeeId, LocalDate effectiveFrom);

    @Query(value =
            "SELECT s.currency_code AS currencyCode, COUNT(*) AS headcount, "
                    + "COALESCE(SUM(s.amount_minor),0) AS totalMinor, COALESCE(AVG(s.amount_minor),0) AS averageMinor "
                    + "FROM " + CURRENT_SALARY + " s "
                    + "INNER JOIN employee e ON e.id = s.employee_id AND e.status = 'ACTIVE' "
                    + "GROUP BY s.currency_code ORDER BY s.currency_code",
            nativeQuery = true)
    List<CurrencyAgg> aggregateByCurrency();

    @Query(value =
            "SELECT e.country_code AS groupKey, COUNT(*) AS headcount, COALESCE(AVG(s.amount_minor),0) AS averageMinor "
                    + "FROM employee e "
                    + "INNER JOIN " + CURRENT_SALARY + " s ON s.employee_id = e.id "
                    + "WHERE e.status = 'ACTIVE' GROUP BY e.country_code ORDER BY e.country_code",
            nativeQuery = true)
    List<GroupAgg> aggregateByCountry();

    @Query(value =
            "SELECT e.department AS groupKey, COUNT(*) AS headcount, COALESCE(AVG(s.amount_minor),0) AS averageMinor "
                    + "FROM employee e "
                    + "INNER JOIN " + CURRENT_SALARY + " s ON s.employee_id = e.id "
                    + "WHERE e.status = 'ACTIVE' GROUP BY e.department ORDER BY e.department",
            nativeQuery = true)
    List<GroupAgg> aggregateByDepartment();

    @Query(value =
            "SELECT CASE WHEN s.amount_minor < 3000000 THEN '0-30k' "
                    + "WHEN s.amount_minor < 5000000 THEN '30k-50k' "
                    + "WHEN s.amount_minor < 8000000 THEN '50k-80k' "
                    + "WHEN s.amount_minor < 12000000 THEN '80k-120k' "
                    + "ELSE '120k+' END AS band, COUNT(*) AS headcount "
                    + "FROM " + CURRENT_SALARY + " s "
                    + "INNER JOIN employee e ON e.id = s.employee_id AND e.status = 'ACTIVE' "
                    + "WHERE (:currencyCode IS NULL OR s.currency_code = :currencyCode) "
                    + "GROUP BY band ORDER BY MIN(s.amount_minor)",
            nativeQuery = true)
    List<BandAgg> distributionByBand(@Param("currencyCode") String currencyCode);

    interface CurrencyAgg {
        String getCurrencyCode();

        long getHeadcount();

        long getTotalMinor();

        double getAverageMinor();
    }

    interface GroupAgg {
        String getGroupKey();

        long getHeadcount();

        double getAverageMinor();
    }

    interface BandAgg {
        String getBand();

        long getHeadcount();
    }
}
