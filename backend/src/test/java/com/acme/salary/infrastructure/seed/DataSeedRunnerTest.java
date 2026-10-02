package com.acme.salary.infrastructure.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acme.salary.infrastructure.config.AppProperties;
import com.acme.salary.infrastructure.persistence.EmployeeRepository;
import com.acme.salary.infrastructure.persistence.HrUserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataSeedRunnerTest {

    private static final int TARGET_COUNT = 5;
    private static final int BATCH_SIZE = 10;

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private HrUserRepository hrUserRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private TransactionStatus transactionStatus;

    private AppProperties appProperties;
    private DataSeedRunner dataSeedRunner;

    @BeforeEach
    void arrange() {
        appProperties = new AppProperties();
        appProperties.getSeed().setEnabled(true);
        appProperties.getSeed().setEmployeeCount(TARGET_COUNT);
        appProperties.getSeed().setBatchSize(BATCH_SIZE);
        appProperties.getSeed().setHrEmail("hr.manager@acme.example");
        appProperties.getSeed().setHrPassword("ChangeMe!Acme2026");

        when(hrUserRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(transactionStatus);

        dataSeedRunner = new DataSeedRunner(
                appProperties, employeeRepository, hrUserRepository, jdbcTemplate, passwordEncoder, transactionManager);
    }

    @Test
    void test_seedEmployees_existingCountMeetsTarget_skipsInsert() {
        // Arrange
        when(employeeRepository.count()).thenReturn((long) TARGET_COUNT);

        // Act
        dataSeedRunner.run(new DefaultApplicationArguments());

        // Assert
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(List.class));
    }

    @Test
    void test_seedEmployees_emptyDatabase_insertsFromZero() {
        // Arrange
        when(employeeRepository.count()).thenReturn(0L);
        AtomicReference<List<Object[]>> empRows = captureEmployeeBatch();

        // Act
        dataSeedRunner.run(new DefaultApplicationArguments());

        // Assert
        List<Object[]> rows = empRows.get();
        assertEquals(TARGET_COUNT, rows.size());
        assertEquals("EMP00000001", rows.get(0)[1]);
        assertEquals("EMP00000005", rows.get(TARGET_COUNT - 1)[1]);
    }

    @Test
    void test_seedEmployees_contiguousPartial_insertsOnlyMissingRange() {
        // Arrange
        int existing = 3;
        when(employeeRepository.count()).thenReturn((long) existing);
        when(jdbcTemplate.queryForObject(eq("SELECT MAX(employee_number) FROM employee"), eq(String.class)))
                .thenReturn(String.format("EMP%08d", existing));
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM salary_record"), eq(Long.class)))
                .thenReturn((long) existing);
        AtomicReference<List<Object[]>> empRows = captureEmployeeBatch();

        // Act
        dataSeedRunner.run(new DefaultApplicationArguments());

        // Assert
        List<Object[]> rows = empRows.get();
        assertEquals(TARGET_COUNT - existing, rows.size());
        assertEquals("EMP00000004", rows.get(0)[1]);
        assertEquals("EMP00000005", rows.get(1)[1]);
        assertTrue(rows.stream().noneMatch(row -> "EMP00000001".equals(row[1])));
    }

    @Test
    void test_seedEmployees_inconsistentPartial_failsWithoutInsert() {
        // Arrange
        when(employeeRepository.count()).thenReturn(3L);
        when(jdbcTemplate.queryForObject(eq("SELECT MAX(employee_number) FROM employee"), eq(String.class)))
                .thenReturn("EMP00000010");
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM salary_record"), eq(Long.class)))
                .thenReturn(3L);

        // Act + Assert
        assertThrows(IllegalStateException.class,
                () -> dataSeedRunner.run(new DefaultApplicationArguments()));
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(List.class));
    }

    @SuppressWarnings("unchecked")
    private AtomicReference<List<Object[]>> captureEmployeeBatch() {
        AtomicReference<List<Object[]>> empRows = new AtomicReference<>();
        ArgumentCaptor<List<Object[]>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        when(jdbcTemplate.batchUpdate(anyString(), rowsCaptor.capture())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.startsWith("INSERT INTO employee")) {
                empRows.set(List.copyOf(rowsCaptor.getValue()));
            }
            List<Object[]> rows = invocation.getArgument(1);
            int[] results = new int[rows.size()];
            Arrays.fill(results, 1);
            return results;
        });
        return empRows;
    }
}
