package com.acme.salary.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acme.salary.domain.HrUser;
import com.acme.salary.infrastructure.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;
    private UUID userId;

    @BeforeEach
    void arrange() {
        AppProperties props = new AppProperties();
        props.getJwt().setSecret("test-secret-key-must-be-at-least-32-chars");
        props.getJwt().setExpirationMs(3_600_000L);
        jwtService = new JwtService(props);
        userId = UUID.randomUUID();
    }

    @Test
    void test_createToken_andParse_roundTripsClaims() {
        // Arrange + Act
        String token = jwtService.createToken(userId, "hr.manager@acme.example", HrUser.ROLE_HR_MANAGER);
        Claims claims = jwtService.parse(token);

        // Assert
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("hr.manager@acme.example", claims.get("email", String.class));
        assertEquals(HrUser.ROLE_HR_MANAGER, claims.get("role", String.class));
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void test_parse_tamperedToken_throwsJwtException() {
        // Arrange
        String token = jwtService.createToken(userId, "hr.manager@acme.example", HrUser.ROLE_HR_MANAGER);
        String tampered = token.substring(0, token.length() - 4) + "xxxx";

        // Act + Assert
        assertThrows(JwtException.class, () -> jwtService.parse(tampered));
    }
}
