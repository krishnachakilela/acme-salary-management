package com.acme.salary.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hr_user")
public class HrUser {
    public static final String ROLE_HR_MANAGER = "HR_MANAGER";
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    @Column(nullable = false, length = 64)
    private String role;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected HrUser() {}

    public HrUser(UUID id, String email, String passwordHash, String role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public UUID getId() {return id;}

    public String getEmail() {return email;}

    public String getPasswordHash() {return passwordHash;}

    public String getRole() {return role;}
}
