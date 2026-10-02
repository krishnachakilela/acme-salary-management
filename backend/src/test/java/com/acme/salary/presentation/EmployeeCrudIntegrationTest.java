package com.acme.salary.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.domain.HrUser;
import com.acme.salary.infrastructure.persistence.EmployeeRepository;
import com.acme.salary.infrastructure.persistence.HrUserRepository;
import com.acme.salary.infrastructure.persistence.SalaryRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeCrudIntegrationTest {

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
    void test_createEmployee_validBody_returnsCreatedWithSalaryHistory() throws Exception {
        // Arrange
        String body = """
        {"employeeNumber":"EMP00000010","firstName":"Alex","lastName":"Smith","email":"alex.smith@acme.example","department":"Engineering","countryCode":"US","currencyCode":"USD","initialSalaryMinor":5000000,"effectiveFrom":"2024-01-01","changeReason":"Initial hire salary"}
        """;

        // Act + Assert
        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employee.employeeNumber").value("EMP00000010"))
                .andExpect(jsonPath("$.employee.currentSalary.amountMinor").value(5_000_000))
                .andExpect(jsonPath("$.salaryHistory.length()").value(1));
    }

    @Test
    void test_createEmployee_duplicateNumber_returnsConflict() throws Exception {
        // Arrange
        createEmployee("EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD");
        String body = """
        {"employeeNumber":"EMP00000010","firstName":"Jordan","lastName":"Lee","email":"jordan.lee@acme.example","department":"Sales","countryCode":"DE","currencyCode":"EUR","initialSalaryMinor":4000000,"effectiveFrom":"2024-01-01","changeReason":"Initial hire salary"}
        """;

        // Act + Assert
        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void test_createEmployee_invalidBody_returnsBadRequest() throws Exception {
        // Arrange
        String body = """
        {"employeeNumber":"BAD","firstName":"","lastName":"","email":"bad","department":"","countryCode":"U","currencyCode":"US","initialSalaryMinor":0,"effectiveFrom":null,"changeReason":""}
        """;

        // Act + Assert
        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_getEmployee_existingId_returnsDetail() throws Exception {
        // Arrange
        String employeeId = createEmployee(
                "EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD");

        // Act + Assert
        mockMvc.perform(get("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee.id").value(employeeId))
                .andExpect(jsonPath("$.employee.email").value("alex.smith@acme.example"));
    }

    @Test
    void test_getEmployee_unknownId_returnsNotFound() throws Exception {
        // Arrange
        UUID unknownId = UUID.randomUUID();

        // Act + Assert
        mockMvc.perform(get("/api/v1/employees/" + unknownId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void test_updateEmployee_validBody_returnsUpdatedDemographics() throws Exception {
        // Arrange
        String employeeId = createEmployee(
                "EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD");
        String body = """
        {"firstName":"Alex","lastName":"Smith","email":"alex.smith@acme.example","department":"Platform","countryCode":"US","currencyCode":"USD","status":"INACTIVE"}
        """;

        // Act + Assert
        mockMvc.perform(put("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee.department").value("Platform"))
                .andExpect(jsonPath("$.employee.status").value("INACTIVE"));
    }

    @Test
    void test_addSalary_validBody_appendsHistoryAndUpdatesCurrent() throws Exception {
        // Arrange
        String employeeId = createEmployee(
                "EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD");
        String body = """
        {"amountMinor":6000000,"effectiveFrom":"2025-01-01","changeReason":"Annual raise"}
        """;

        // Act + Assert
        mockMvc.perform(post("/api/v1/employees/" + employeeId + "/salaries")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employee.currentSalary.amountMinor").value(6_000_000))
                .andExpect(jsonPath("$.salaryHistory.length()").value(2));
    }

    @Test
    void test_addSalary_inactiveEmployee_returnsConflict() throws Exception {
        // Arrange
        String employeeId = createEmployee(
                "EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD");
        mockMvc.perform(put("/api/v1/employees/" + employeeId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"firstName":"Alex","lastName":"Smith","email":"alex.smith@acme.example","department":"Engineering","countryCode":"US","currencyCode":"USD","status":"INACTIVE"}
                """))
                .andExpect(status().isOk());
        String body = """
        {"amountMinor":6000000,"effectiveFrom":"2025-01-01","changeReason":"Annual raise"}
        """;

        // Act + Assert
        mockMvc.perform(post("/api/v1/employees/" + employeeId + "/salaries")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Salary changes are allowed only for active employees"));
    }
    private String login() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + HR_EMAIL + "\",\"password\":\"" + HR_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String createEmployee(
            String number,
            String firstName,
            String lastName,
            String email,
            String department,
            String countryCode,
            String currencyCode) throws Exception {
        String body = """
        {"employeeNumber":"%s","firstName":"%s","lastName":"%s","email":"%s","department":"%s","countryCode":"%s","currencyCode":"%s","initialSalaryMinor":5000000,"effectiveFrom":"2024-01-01","changeReason":"Initial hire salary"}
        """.formatted(number, firstName, lastName, email, department, countryCode, currencyCode);
        MvcResult created = mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode root = objectMapper.readTree(created.getResponse().getContentAsString());
        return root.get("employee").get("id").asText();
    }
}
