CREATE TABLE hr_user (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_hr_user_email UNIQUE (email)
);

CREATE TABLE employee (
    id               UUID PRIMARY KEY,
    employee_number  VARCHAR(32)  NOT NULL,
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    department       VARCHAR(100) NOT NULL,
    country_code     VARCHAR(2)   NOT NULL,
    currency_code    VARCHAR(3)   NOT NULL,
    status           VARCHAR(32)  NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_employee_number UNIQUE (employee_number),
    CONSTRAINT uq_employee_email UNIQUE (email),
    CONSTRAINT ck_employee_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE salary_record (
    id              UUID PRIMARY KEY,
    employee_id     UUID         NOT NULL REFERENCES employee (id) ON DELETE CASCADE,
    amount_minor    BIGINT       NOT NULL,
    currency_code   VARCHAR(3)   NOT NULL,
    effective_from  DATE         NOT NULL,
    change_reason   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_salary_amount_positive CHECK (amount_minor > 0)
);

CREATE INDEX idx_employee_name ON employee (last_name, first_name);
CREATE INDEX idx_employee_department ON employee (department);
CREATE INDEX idx_employee_country ON employee (country_code);
CREATE INDEX idx_employee_status ON employee (status);
CREATE INDEX idx_salary_employee_effective ON salary_record (employee_id, effective_from DESC);