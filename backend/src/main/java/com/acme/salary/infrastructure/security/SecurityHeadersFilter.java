package com.acme.salary.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final String CSP_DEFAULT = "default-src 'self'";
    // Swagger UI needs inline script/style; scoped only to docs paths
    private static final String CSP_SWAGGER =
            "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; "
                    + "img-src 'self' data:; object-src 'none'; frame-ancestors 'none'";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy", resolveCsp(request.getRequestURI()));
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        filterChain.doFilter(request, response);
    }

    private static String resolveCsp(String requestUri) {
        if (isSwaggerPath(requestUri)) {
            return CSP_SWAGGER;
        }
        return CSP_DEFAULT;
    }

    private static boolean isSwaggerPath(String requestUri) {
        return requestUri != null
                && (requestUri.startsWith("/swagger-ui")
                || requestUri.startsWith("/v3/api-docs"));
    }
}
