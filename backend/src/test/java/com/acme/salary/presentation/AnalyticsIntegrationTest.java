package com.acme.salary.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.domain.HrUser;
import com.acme.salary.infrastructure.persistence.EmployeeRepository;
import com.acme.salary.infrastructure.persistence.HrUserRepository;
import com.acme.salary.infrastructure.persistence.SalaryRecordRepository;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsIntegrationTest {

    private static final String HR_EMAIL = "hr.manager@acme.example";
    private static final String HR_PASSWORD = "ChangeMe!Acme2026";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private HrUserRepository hrUserRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SalaryRecordRepository salaryRecordRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;

    @BeforeEach
    void arrangeHrUser() throws Exception {
        salaryRecordRepository.deleteAll();
        employeeRepository.deleteAll();
        hrUserRepository.deleteAll();
        hrUserRepository.save(new HrUser(
                UUID.randomUUID(),
                HR_EMAIL,
                passwordEncoder.encode(HR_PASSWORD),
                HrUser.ROLE_HR_MANAGER,
                Instant.now()));
        accessToken = login();
    }

    @Test
    void test_summary_withEmployees_returnsAggregates() throws Exception {
        // Arrange
        createEmployee("EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD", 5_000_000L);
        createEmployee("EMP00000020", "Zoe", "Young", "zoe.young@acme.example", "Sales", "DE", "EUR", 4_000_000L);

        // Act + Assert
        mockMvc.perform(get("/api/v1/analytics/summary")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployees").value(2))
                .andExpect(jsonPath("$.byCurrency").isArray())
                .andExpect(jsonPath("$.byCountry").isArray())
                .andExpect(jsonPath("$.byDepartment").isArray());
    }

    @Test
    void test_distribution_withCurrencyFilter_returnsBands() throws Exception {
        // Arrange
        createEmployee("EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD", 5_000_000L);
        createEmployee("EMP00000020", "Zoe", "Young", "zoe.young@acme.example", "Sales", "DE", "EUR", 4_000_000L);

        // Act + Assert
        mockMvc.perform(get("/api/v1/analytics/distribution")
                        .param("currencyCode", "USD")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyCode").value("USD"))
                .andExpect(jsonPath("$.bands").isArray())
                .andExpect(jsonPath("$.bands[0].band").exists())
                .andExpect(jsonPath("$.bands[0].headcount").value(1));
    }

    @Test
    void test_summary_withoutToken_returnsUnauthorized() throws Exception {
        // Act + Assert
        mockMvc.perform(get("/api/v1/analytics/summary"))
                .andExpect(status().isUnauthorized());
    }

    private String login() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + HR_EMAIL + "\",\"password\":\"" + HR_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private void createEmployee(
            String number,
            String firstName,
            String lastName,
            String email,
            String department,
            String countryCode,
            String currencyCode,
            long salaryMinor) throws Exception {
        String body = """
        {"employeeNumber":"%s","firstName":"%s","lastName":"%s","email":"%s","department":"%s","countryCode":"%s","currencyCode":"%s","initialSalaryMinor":%d,"effectiveFrom":"2024-01-01","changeReason":"Initial hire salary"}
        """.formatted(number, firstName, lastName, email, department, countryCode, currencyCode, salaryMinor);
        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
 