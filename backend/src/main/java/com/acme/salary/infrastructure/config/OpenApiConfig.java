package com.acme.salary.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI acmeSalaryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ACME Salary Management API")
                        .version("1.0.0")
                        .description("HR API for employee search, salary updates, and compensation analytics."))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_AUTH_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT from POST /api/v1/auth/login")));
    }
}
