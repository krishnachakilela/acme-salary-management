package com.acme.salary.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();
    private final Seed seed = new Seed();

    public Jwt getJwt() {return jwt;}

    public Cors getCors() {return cors;}

    public Seed getSeed() {return seed;}

    public static class Jwt {
        private String secret = "change-me-to-a-long-random-secret-at-least-32-chars";
        private long expirationMs = 28800000L;

        public String getSecret() {return secret;}

        public void setSecret(String s) {this.secret = s;}

        public long getExpirationMs() {return expirationMs;}

        public void setExpirationMs(long v) {this.expirationMs = v;}
    }

    public static class Cors {
        private String allowedOrigins = "http://localhost:8080,http://localhost:4200";

        public String getAllowedOrigins() {return allowedOrigins;}

        public void setAllowedOrigins(String v) {this.allowedOrigins = v;}
    }

    public static class Seed {
        private boolean enabled;
        private int employeeCount = 10000;
        private int batchSize = 500;
        private String hrEmail = "hr.manager@acme.example";
        private String hrPassword = "ChangeMe!Acme2026";

        public boolean isEnabled() {return enabled;}

        public void setEnabled(boolean v) {this.enabled = v;}

        public int getEmployeeCount() {return employeeCount;}

        public void setEmployeeCount(int v) {this.employeeCount = v;}

        public int getBatchSize() {return batchSize;}

        public void setBatchSize(int v) {this.batchSize = v;}

        public String getHrEmail() {return hrEmail;}

        public void setHrEmail(String v) {this.hrEmail = v;}

        public String getHrPassword() {return hrPassword;}

        public void setHrPassword(String v) {this.hrPassword = v;}
    }
}
