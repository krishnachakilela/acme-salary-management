package com.acme.salary.infrastructure.persistence;

import com.acme.salary.domain.SalaryRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, UUID> {
    List<SalaryRecord> findByEmployeeIdOrderByEffectiveFromDesc(UUID employeeId);

    Optional<SalaryRecord> findFirstByEmployeeIdOrderByEffectiveFromDesc(UUID employeeId);

    @Query(value = """
      SELECT s.currency_code AS currencyCode, COUNT(*) AS headcount,
             COALESCE(SUM(s.amount_minor),0) AS totalMinor, COALESCE(AVG(s.amount_minor),0) AS averageMinor
      FROM salary_record s
      INNER JOIN (SELECT employee_id, MAX(effective_from) AS max_from FROM salary_record GROUP BY employee_id) latest
        ON latest.employee_id = s.employee_id AND latest.max_from = s.effective_from
      INNER JOIN employee e ON e.id = s.employee_id AND e.status = 'ACTIVE'
      GROUP BY s.currency_code ORDER BY s.currency_code
      """, nativeQuery = true)
    List<CurrencyAgg> aggregateByCurrency();

    @Query(value = """
      SELECT e.country_code AS groupKey, COUNT(*) AS headcount, COALESCE(AVG(s.amount_minor),0) AS averageMinor
      FROM employee e
      INNER JOIN salary_record s ON s.employee_id = e.id
      INNER JOIN (SELECT employee_id, MAX(effective_from) AS max_from FROM salary_record GROUP BY employee_id) latest
        ON latest.employee_id = s.employee_id AND latest.max_from = s.effective_from
      WHERE e.status = 'ACTIVE' GROUP BY e.country_code ORDER BY e.country_code
      """, nativeQuery = true)
    List<GroupAgg> aggregateByCountry();

    @Query(value = """
      SELECT e.department AS groupKey, COUNT(*) AS headcount, COALESCE(AVG(s.amount_minor),0) AS averageMinor
      FROM employee e
      INNER JOIN salary_record s ON s.employee_id = e.id
      INNER JOIN (SELECT employee_id, MAX(effective_from) AS max_from FROM salary_record GROUP BY employee_id) latest
        ON latest.employee_id = s.employee_id AND latest.max_from = s.effective_from
      WHERE e.status = 'ACTIVE' GROUP BY e.department ORDER BY e.department
      """, nativeQuery = true)
    List<GroupAgg> aggregateByDepartment();

    @Query(value = """
      SELECT CASE WHEN s.amount_minor < 3000000 THEN '0-30k'
                  WHEN s.amount_minor < 5000000 THEN '30k-50k'
                  WHEN s.amount_minor < 8000000 THEN '50k-80k'
                  WHEN s.amount_minor < 12000000 THEN '80k-120k'
                  ELSE '120k+' END AS band, COUNT(*) AS headcount
      FROM salary_record s
      INNER JOIN (SELECT employee_id, MAX(effective_from) AS max_from FROM salary_record GROUP BY employee_id) latest
        ON latest.employee_id = s.employee_id AND latest.max_from = s.effective_from
      INNER JOIN employee e ON e.id = s.employee_id AND e.status = 'ACTIVE'
      WHERE (:currencyCode IS NULL OR s.currency_code = :currencyCode)
      GROUP BY band ORDER BY MIN(s.amount_minor)
      """, nativeQuery = true)
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
