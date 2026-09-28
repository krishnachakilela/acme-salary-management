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
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeedRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeedRunner.class);
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

    public DataSeedRunner(
            AppProperties appProperties, EmployeeRepository employeeRepository, HrUserRepository hrUserRepository,
            JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.appProperties = appProperties;
        this.employeeRepository = employeeRepository;
        this.hrUserRepository = hrUserRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
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
        hrUserRepository.save(new HrUser(UUID.randomUUID(), email.toLowerCase(),
                passwordEncoder.encode(appProperties.getSeed().getHrPassword()), HrUser.ROLE_HR_MANAGER, Instant.now()));
        log.info("event=hr_user_seeded");
    }

    private void seedEmployees() {
        int target = appProperties.getSeed().getEmployeeCount();
        if (employeeRepository.count() >= target) {
            log.info("event=seed_skipped reason=already_seeded");
            return;
        }
        int batch = Math.max(1, appProperties.getSeed().getBatchSize());
        Random random = new Random(42L);
        Timestamp now = Timestamp.from(Instant.now());
        long started = System.currentTimeMillis();
        log.info("event=seed_start remaining={}", target);
        for (int start = 0; start < target; start += batch) {
            int end = Math.min(start + batch, target);
            List<Object[]> empRows = new ArrayList<>(end - start);
            List<Object[]> salRows = new ArrayList<>(end - start);
            for (int i = start; i < end; i++) {
                UUID id = UUID.randomUUID();
                String[] cc = COUNTRIES[i % COUNTRIES.length];
                long amount = (40_000L + random.nextInt(160_000)) * 100L;
                if ("INR".equals(cc[1]))
                    amount = (40_000L + random.nextInt(160_000)) * 80L;
                empRows.add(new Object[] {
                        id, String.format("EMP%08d", i + 1), FIRST[random.nextInt(FIRST.length)], LAST[random.nextInt(LAST.length)],
                        ("emp" + (i + 1) + "@acme.example").toLowerCase(), DEPTS[i % DEPTS.length], cc[0], cc[1], "ACTIVE", now, now
                });
                salRows.add(new Object[] {
                        UUID.randomUUID(), id, amount, cc[1], Date.valueOf(LocalDate.of(2024, 1, 1).plusDays(i % 365)),
                        "Initial hire salary", now
                });
            }
            jdbcTemplate.batchUpdate(
                    "INSERT INTO employee (id,employee_number,first_name,last_name,email,department,country_code,currency_code,status,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                    empRows);
            jdbcTemplate.batchUpdate(
                    "INSERT INTO salary_record (id,employee_id,amount_minor,currency_code,effective_from,change_reason,created_at) VALUES (?,?,?,?,?,?,?)",
                    salRows);
        }
        log.info("event=seed_complete durationMs={}", System.currentTimeMillis() - started);
    }
}
