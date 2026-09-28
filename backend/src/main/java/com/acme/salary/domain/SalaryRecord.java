package com.acme.salary.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "salary_record")
public class SalaryRecord {
    @Id
    private UUID id;
    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;
    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
    @Column(name = "change_reason", nullable = false, length = 255)
    private String changeReason;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SalaryRecord() {}

    public SalaryRecord(
            UUID id,
            UUID employeeId,
            Money money,
            LocalDate effectiveFrom,
            String changeReason,
            Instant createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.amountMinor = money.amountMinor();
        this.currencyCode = money.currencyCode();
        this.effectiveFrom = effectiveFrom;
        if (changeReason == null || changeReason.isBlank() || changeReason.trim().length() > 255)
            throw new IllegalArgumentException("Invalid reason");
        this.changeReason = changeReason.trim();
        this.createdAt = createdAt;
    }

    public static SalaryRecord create(UUID employeeId, Money money, LocalDate effectiveFrom, String changeReason) {
        return new SalaryRecord(UUID.randomUUID(), employeeId, money, effectiveFrom, changeReason, Instant.now());
    }

    public UUID getId() {return id;}

    public UUID getEmployeeId() {return employeeId;}

    public long getAmountMinor() {return amountMinor;}

    public String getCurrencyCode() {return currencyCode;}

    public LocalDate getEffectiveFrom() {return effectiveFrom;}

    public String getChangeReason() {return changeReason;}

    public Instant getCreatedAt() {return createdAt;}
}
