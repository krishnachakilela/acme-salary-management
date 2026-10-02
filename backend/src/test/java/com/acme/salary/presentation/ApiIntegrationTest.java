package com.acme.salary.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.domain.HrUser;
import com.acme.salary.infrastructure.persistence.HrUserRepository;
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
class ApiIntegrationTest {

    private static final String HR_EMAIL = "hr.manager@acme.example";
    private static final String HR_PASSWORD = "ChangeMe!Acme2026";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private HrUserRepository hrUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedUser() {
        hrUserRepository.deleteAll();
        hrUserRepository.save(new HrUser(
                UUID.randomUUID(),
                HR_EMAIL,
                passwordEncoder.encode(HR_PASSWORD),
                HrUser.ROLE_HR_MANAGER,
                Instant.now()));
    }

    @Test
    void test_health_returnsUp() throws Exception {
        // Act + Assert
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void test_employees_withoutToken_returnsUnauthorized() throws Exception {
        // Act + Assert
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_login_andListEmployees_returnsOk() throws Exception {
        // Arrange
        String body = "{\"email\":\"" + HR_EMAIL + "\",\"password\":\"" + HR_PASSWORD + "\"}";

        // Act
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();

        // Assert
        mockMvc.perform(get("/api/v1/employees?page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void test_login_invalidBody_returnsBadRequest() throws Exception {
        // Arrange
        String body = "{\"email\":\"not-an-email\",\"password\":\"short\"}";

        // Act + Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_login_wrongPassword_returnsUnauthorized() throws Exception {
        // Arrange
        String body = "{\"email\":\"" + HR_EMAIL + "\",\"password\":\"WrongPassword123\"}";

        // Act + Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void test_apiDocs_withoutToken_returnsOpenApiDocument() throws Exception {
        // Act + Assert
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("ACME Salary Management API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"));
    }

    @Test
    void test_swaggerUi_withoutToken_returnsOk() throws Exception {
        // Act + Assert
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
