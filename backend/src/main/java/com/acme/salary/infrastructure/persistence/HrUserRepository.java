package com.acme.salary.infrastructure.persistence;

import com.acme.salary.domain.HrUser;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HrUserRepository extends JpaRepository<HrUser, UUID> {
    Optional<HrUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
