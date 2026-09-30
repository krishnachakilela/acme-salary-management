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
class EmployeeSearchIntegrationTest {
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
        hrUserRepository.save(new HrUser(UUID.randomUUID(), HR_EMAIL,
                passwordEncoder.encode(HR_PASSWORD), HrUser.ROLE_HR_MANAGER, Instant.now()));
        accessToken = login();
    }

    @Test
    void test_search_withoutFilters_ordersByEmployeeNumber() throws Exception {
        // Arrange
        createEmployee("EMP00000030", "Zoe", "Young", "zoe.young@acme.example", "Sales", "DE", "EUR");
        createEmployee("EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD");
        createEmployee("EMP00000020", "Jordan", "Brown", "jordan.brown@acme.example", "HR", "JP", "JPY");

        // Act
        MvcResult result = mockMvc.perform(get("/api/v1/employees?page=0&size=20")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        JsonNode content = objectMapper.readTree(result.getResponse().getContentAsString()).get("content");
        org.junit.jupiter.api.Assertions.assertEquals(3, content.size());
        org.junit.jupiter.api.Assertions.assertEquals("EMP00000010", content.get(0).get("employeeNumber").asText());
        org.junit.jupiter.api.Assertions.assertEquals("EMP00000020", content.get(1).get("employeeNumber").asText());
        org.junit.jupiter.api.Assertions.assertEquals("EMP00000030", content.get(2).get("employeeNumber").asText());
    }

    @Test
    void test_search_byName_returnsOnlyMatches() throws Exception {
        // Arrange
        createEmployee("EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD");
        createEmployee("EMP00000020", "Zoe", "Young", "zoe.young@acme.example", "Sales", "DE", "EUR");

        // Act + Assert
        mockMvc.perform(get("/api/v1/employees").param("name", "Young, Zoe")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].employeeNumber").value("EMP00000020"));
    }

    @Test
    void test_search_byDepartment_ignoresCase() throws Exception {
        // Arrange
        createEmployee("EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD");
        createEmployee("EMP00000020", "Zoe", "Young", "zoe.young@acme.example", "Sales", "DE", "EUR");

        // Act + Assert
        mockMvc.perform(get("/api/v1/employees").param("department", "sales")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].department").value("Sales"));
    }

    @Test
    void test_search_byCountryCode_returnsOnlyMatches() throws Exception {
        // Arrange
        createEmployee("EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD");
        createEmployee("EMP00000020", "Zoe", "Young", "zoe.young@acme.example", "Sales", "DE", "EUR");

        // Act + Assert
        mockMvc.perform(get("/api/v1/employees").param("countryCode", "de")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].countryCode").value("DE"));
    }

    @Test
    void test_search_byStatus_returnsOnlyInactive() throws Exception {
        // Arrange
        String activeId = createEmployee("EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US",
                "USD");
        createEmployee("EMP00000020", "Zoe", "Young", "zoe.young@acme.example", "Sales", "DE", "EUR");
        mockMvc.perform(put("/api/v1/employees/" + activeId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"firstName":"Alex","lastName":"Smith","email":"alex.smith@acme.example","department":"Engineering","countryCode":"US","currencyCode":"USD","status":"INACTIVE"}
                """))
                .andExpect(status().isOk());

        // Act + Assert
        mockMvc.perform(get("/api/v1/employees").param("status", "INACTIVE")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].employeeNumber").value("EMP00000010"))
                .andExpect(jsonPath("$.content[0].status").value("INACTIVE"));
    }

    @Test
    void test_search_unknownStatus_returnsBadRequest() throws Exception {
        // Act + Assert
        mockMvc.perform(get("/api/v1/employees").param("status", "NOT_A_STATUS")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_search_likeWildcard_doesNotMatchAll() throws Exception {
        // Arrange
        createEmployee("EMP00000010", "Alex", "Smith", "alex.smith@acme.example", "Engineering", "US", "USD");
        createEmployee("EMP00000020", "Zoe", "Young", "zoe.young@acme.example", "Sales", "DE", "EUR");

        // Act + Assert
        mockMvc.perform(get("/api/v1/employees").param("name", "%")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
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
            String number, String firstName, String lastName, String email,
            String department, String countryCode, String currencyCode) throws Exception {
        String body = """
        {"employeeNumber":"%s","firstName":"%s","lastName":"%s","email":"%s","department":"%s","countryCode":"%s","currencyCode":"%s","initialSalaryMinor":5000000,"effectiveFrom":"2024-01-01","changeReason":"Initial hire salary"}
        """.formatted(number, firstName, lastName, email, department, countryCode, currencyCode);
        MvcResult created = mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(created.getResponse().getContentAsString()).get("employee").get("id").asText();
    }
}
