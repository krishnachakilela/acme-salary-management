package com.acme.salary.presentation.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(@NotBlank @Email @Size(max = 255) String email,
                           @NotBlank @Size(min = 8, max = 128) String password) {}
