package com.acme.salary.presentation.dto;

public record LoginResponse(String accessToken, String tokenType, String role, String email) {}
