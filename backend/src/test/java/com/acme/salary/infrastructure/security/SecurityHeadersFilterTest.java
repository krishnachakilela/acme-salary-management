package com.acme.salary.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityHeadersFilterTest {

    @Test
    void test_doFilterInternal_apiPath_setsStrictCsp() throws Exception {
        // Arrange
        SecurityHeadersFilter filter = new SecurityHeadersFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilter(request, response, new MockFilterChain());

        // Assert
        assertEquals("default-src 'self'", response.getHeader("Content-Security-Policy"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
    }

    @Test
    void test_doFilterInternal_swaggerPath_allowsInlineAssets() throws Exception {
        // Arrange
        SecurityHeadersFilter filter = new SecurityHeadersFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilter(request, response, new MockFilterChain());

        // Assert
        String csp = response.getHeader("Content-Security-Policy");
        assertTrue(csp.contains("script-src 'self' 'unsafe-inline'"));
        assertTrue(csp.contains("style-src 'self' 'unsafe-inline'"));
    }
}
