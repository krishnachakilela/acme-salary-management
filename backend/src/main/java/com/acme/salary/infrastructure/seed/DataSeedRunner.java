package com.acme.salary.infrastructure.seed;

import com.acme.salary.domain.HrUser;
import com.acme.salary.infrastructure.config.AppProperties;
import com.acme.salary.infrastructure.persistence.EmployeeRepository;
import com.acme.salary.infrastructure.persistence.HrUserRepository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class DataSeedRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeedRunner.class);
    private static final long RANDOM_SEED = 42L;
    private static final int MIN_BASE_SALARY = 40_000;
    private static final int SALARY_RANGE = 160_000;
    private static final long NON_INR_MINOR_FACTOR = 100L;
    private static final long INR_MINOR_FACTOR = 80L;
    private static final String EMPLOYEE_NUMBER_FORMAT = "EMP%08d";
    private static final String[][] COUNTRIES = {
            {"US", "USD"}, {"GB", "GBP"}, {"DE", "EUR"}, {"IN", "INR"}, {"JP", "JPY"}, {"CA", "CAD"}, {"AU", "AUD"},
            {"FR", "EUR"}, {"BR", "BRL"}, {"SG", "SGD"}, {"NL", "EUR"}, {"IE", "EUR"}
    };
    private static final String[] DEPTS = {
            "Engineering", "Sales", "HR", "Finance", "Operations", "Marketing", "Support", "Legal"
    };
    private static final String[] FIRST = {
            "Alex", "Jordan", "Taylor", "Morgan", "Casey", "Riley", "Avery", "Quinn", "Sam", "Jamie", "Cameron", "Drew",
            "Harper", "Reese", "Skyler", "Parker"
    };
    private static final String[] LAST = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
            "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas"
    };
    private final AppProperties appProperties;
    private final EmployeeRepository employeeRepository;
    private final HrUserRepository hrUserRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    public DataSeedRunner(
            AppProperties appProperties, EmployeeRepository employeeRepository, HrUserRepository hrUserRepository,
            JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, PlatformTransactionManager transactionManager) {
        this.appProperties = appProperties;
        this.employeeRepository = employeeRepository;
        this.hrUserRepository = hrUserRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        seedHr();
        if (!appProperties.getSeed().isEnabled()) {
            log.info("event=seed_skipped reason=disabled");
            return;
        }
        seedEmployees();
    }

    private void seedHr() {
        String email = appProperties.getSeed().getHrEmail();
        if (hrUserRepository.existsByEmailIgnoreCase(email))
            return;
        transactionTemplate.executeWithoutResult(status -> hrUserRepository.save(new HrUser(
                UUID.randomUUID(), email.toLowerCase(),
                passwordEncoder.encode(appProperties.getSeed().getHrPassword()), HrUser.ROLE_HR_MANAGER, Instant.now())));
        log.info("event=hr_user_seeded");
    }

    private void seedEmployees() {
        int target = appProperties.getSeed().getEmployeeCount();
        int existing = Math.toIntExact(employeeRepository.count());
        if (existing >= target) {
            log.info("event=seed_skipped reason=already_seeded");
            return;
        }
        if (existing > 0) {
            assertContiguousSeedPrefix(existing);
        }

        int batch = Math.max(1, appProperties.getSeed().getBatchSize());
        Random random = new Random(RANDOM_SEED);
        advanceRandom(random, existing);
        Timestamp now = Timestamp.from(Instant.now());
        long started = System.currentTimeMillis();
        int remaining = target - existing;
        log.info("event=seed_start existing={} remaining={} resumeFrom={}", existing, remaining, existing);
        for (int start = existing; start < target; start += batch) {
            int end = Math.min(start + batch, target);
            List<Object[]> empRows = new ArrayList<>(end - start);
            List<Object[]> salRows = new ArrayList<>(end - start);
            for (int i = start; i < end; i++) {
                UUID id = UUID.randomUUID();
                String[] cc = COUNTRIES[i % COUNTRIES.length];
                String firstName = FIRST[i % FIRST.length];
                String lastName = LAST[(i / FIRST.length) % LAST.length];
                long amount = nextSalaryAmountMinor(random, cc[1]);
                empRows.add(new Object[] {
                        id, String.format(EMPLOYEE_NUMBER_FORMAT, i + 1), firstName, lastName,
                        ("emp" + (i + 1) + "@acme.example").toLowerCase(), DEPTS[i % DEPTS.length], cc[0], cc[1], "ACTIVE", now, now
                });
                salRows.add(new Object[] {
                        UUID.randomUUID(), id, amount, cc[1], Date.valueOf(LocalDate.of(2024, 1, 1).plusDays(i % 365)),
                        "Initial hire salary", now
                });
            }
            transactionTemplate.executeWithoutResult(status -> {
                jdbcTemplate.batchUpdate(
                        "INSERT INTO employee (id,employee_number,first_name,last_name,email,department,country_code,currency_code,status,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                        empRows);
                jdbcTemplate.batchUpdate(
                        "INSERT INTO salary_record (id,employee_id,amount_minor,currency_code,effective_from,change_reason,created_at) VALUES (?,?,?,?,?,?,?)",
                        salRows);
            });
        }
        log.info("event=seed_complete durationMs={}", System.currentTimeMillis() - started);
    }

    private void assertContiguousSeedPrefix(int existingCount) {
        String maxEmployeeNumber = jdbcTemplate.queryForObject(
                "SELECT MAX(employee_number) FROM employee", String.class);
        Long salaryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM salary_record", Long.class);
        String expectedMax = String.format(EMPLOYEE_NUMBER_FORMAT, existingCount);
        boolean isContiguousPrefix = expectedMax.equals(maxEmployeeNumber)
                && salaryCount != null
                && salaryCount == existingCount;
        if (isContiguousPrefix)
            return;
        throw new IllegalStateException(
                "Partial seed data is inconsistent (expected contiguous "
                        + String.format(EMPLOYEE_NUMBER_FORMAT, 1)
                        + ".."
                        + expectedMax
                        + " with matching salary_record rows). Truncate employee and salary_record, then restart seeding.");
    }

    private static void advanceRandom(Random random, int upToExclusive) {
        for (int i = 0; i < upToExclusive; i++) {
            nextSalaryAmountMinor(random, COUNTRIES[i % COUNTRIES.length][1]);
        }
    }

    private static long nextSalaryAmountMinor(Random random, String currencyCode) {
        long amount = (MIN_BASE_SALARY + random.nextInt(SALARY_RANGE)) * NON_INR_MINOR_FACTOR;
        if ("INR".equals(currencyCode))
            amount = (MIN_BASE_SALARY + random.nextInt(SALARY_RANGE)) * INR_MINOR_FACTOR;
        return amount;
    }
}
