package com.acme.salary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acme.salary.domain.HrUser;
import com.acme.salary.exception.UnauthorizedException;
import com.acme.salary.infrastructure.persistence.HrUserRepository;
import com.acme.salary.infrastructure.security.JwtService;
import com.acme.salary.presentation.dto.LoginRequest;
import com.acme.salary.presentation.dto.LoginResponse;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private HrUserRepository hrUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthService authService;
    private HrUser hrUser;

    @BeforeEach
    void arrange() {
        authService = new AuthService(hrUserRepository, passwordEncoder, jwtService);
        hrUser = new HrUser(
                UUID.randomUUID(),
                "hr.manager@acme.example",
                "hashed-password",
                HrUser.ROLE_HR_MANAGER,
                Instant.now());
    }

    @Test
    void test_login_validCredentials_returnsTokenAndRole() {
        // Arrange
        LoginRequest request = new LoginRequest("hr.manager@acme.example", "ChangeMe!Acme2026");
        when(hrUserRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(hrUser));
        when(passwordEncoder.matches(request.password(), hrUser.getPasswordHash())).thenReturn(true);
        when(jwtService.createToken(hrUser.getId(), hrUser.getEmail(), hrUser.getRole()))
                .thenReturn("jwt-token");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(HrUser.ROLE_HR_MANAGER, response.role());
        assertEquals(hrUser.getEmail(), response.email());
        verify(jwtService).createToken(hrUser.getId(), hrUser.getEmail(), hrUser.getRole());
    }

    @Test
    void test_login_unknownUser_throwsUnauthorizedException() {
        // Arrange
        LoginRequest request = new LoginRequest("unknown@acme.example", "whatever");
        when(hrUserRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void test_login_badPassword_throwsUnauthorizedException() {
        // Arrange
        LoginRequest request = new LoginRequest("hr.manager@acme.example", "wrong-password");
        when(hrUserRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(hrUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act + Assert
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }
}
