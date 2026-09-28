package com.acme.salary.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "employee")
public class Employee {
    private static final Pattern EMP_NO = Pattern.compile("^EMP[0-9]{4,10}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    @Id
    private UUID id;
    @Column(name = "employee_number", nullable = false, unique = true, length = 32)
    private String employeeNumber;
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    @Column(nullable = false, length = 100)
    private String department;
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EmployeeStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Employee() {}

    public Employee(
            UUID id, String employeeNumber, String firstName, String lastName, String email,
            String department, CountryCode countryCode, String currencyCode, EmployeeStatus status, Instant createdAt) {
        this.id = id;
        if (employeeNumber == null || !EMP_NO.matcher(employeeNumber).matches())
            throw new IllegalArgumentException("Invalid employee number");
        this.employeeNumber = employeeNumber;
        this.firstName = require(firstName);
        this.lastName = require(lastName);
        this.email = requireEmail(email);
        this.department = require(department);
        this.countryCode = countryCode.value();
        this.currencyCode = new Money(1L, currencyCode).currencyCode();
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static Employee create(
            String employeeNumber, String firstName, String lastName, String email,
            String department, CountryCode countryCode, String currencyCode) {
        return new Employee(UUID.randomUUID(), employeeNumber, firstName, lastName, email, department, countryCode,
                currencyCode, EmployeeStatus.ACTIVE, Instant.now());
    }

    public void updateDemographics(
            String firstName, String lastName, String email, String department,
            CountryCode countryCode, String currencyCode, EmployeeStatus status) {
        this.firstName = require(firstName);
        this.lastName = require(lastName);
        this.email = requireEmail(email);
        this.department = require(department);
        this.countryCode = countryCode.value();
        this.currencyCode = new Money(1L, currencyCode).currencyCode();
        this.status = status;
        this.updatedAt = Instant.now();
    }

    private static String require(String v) {
        if (v == null || v.isBlank() || v.trim().length() > 100)
            throw new IllegalArgumentException("Invalid text");
        return v.trim();
    }

    private static String requireEmail(String v) {
        String n = require(v).toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(n).matches())
            throw new IllegalArgumentException("Invalid email");
        return n;
    }

    public UUID getId() {return id;}

    public String getEmployeeNumber() {return employeeNumber;}

    public String getFirstName() {return firstName;}

    public String getLastName() {return lastName;}

    public String getEmail() {return email;}

    public String getDepartment() {return department;}

    public String getCountryCode() {return countryCode;}

    public String getCurrencyCode() {return currencyCode;}

    public EmployeeStatus getStatus() {return status;}

    public Instant getCreatedAt() {return createdAt;}

    public Instant getUpdatedAt() {return updatedAt;}
}
