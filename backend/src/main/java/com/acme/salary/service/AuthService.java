package com.acme.salary.service;

import com.acme.salary.domain.HrUser;
import com.acme.salary.exception.UnauthorizedException;
import com.acme.salary.infrastructure.persistence.HrUserRepository;
import com.acme.salary.infrastructure.security.JwtService;
import com.acme.salary.presentation.dto.LoginRequest;
import com.acme.salary.presentation.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final HrUserRepository hrUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            HrUserRepository hrUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.hrUserRepository = hrUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        HrUser user = hrUserRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> {
                    log.info("event=auth_failed reason=unknown_user");
                    return new UnauthorizedException("Invalid credentials");
                });
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.info("event=auth_failed reason=bad_password");
            throw new UnauthorizedException("Invalid credentials");
        }
        log.info("event=auth_success role={}", user.getRole());
        return new LoginResponse(
                jwtService.createToken(user.getId(), user.getEmail(), user.getRole()),
                "Bearer",
                user.getRole(),
                user.getEmail()
        );
    }
}
