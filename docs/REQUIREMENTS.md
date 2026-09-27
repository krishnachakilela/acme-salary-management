# ACME Employee Salary Management — Requirements

## Goal

Replace spreadsheet-based salary tracking with a local, searchable system that lets ACME HR manage 10,000+ employee compensation records and answer org-level pay questions through dashboards.

## Persona

**HR Manager** — sole user of the system. Authenticates with demo credentials, manages employee demographics and salary history, and reviews compensation analytics.

## Problem

At ~10k employees, Excel becomes slow, error-prone, and hard to filter or aggregate by country, department, or salary band. HR needs pagination, validated CRUD, and SQL-backed analytics without introducing a full payroll engine.

## In Scope

| Capability | Description |
|------------|-------------|
| Auth | Simple JWT login for a single HR Manager role |
| Employee CRUD | Create, read, update employees (demographics, country, currency, status) |
| Salary history | Append salary changes; derive current salary from latest effective date |
| Search / filter | Name, department, country, status with mandatory pagination |
| Analytics | Headcount, total/average pay by currency, distribution by country/department and salary bands |
| Seed data | Deterministic 10,000 employees plus one demo HR user |
| Deploy | Docker Compose on localhost (`postgres` + `api` + `ui`) |

## Out of Scope

| Left out | Reasoning |
|----------|-----------|
| Payroll runs, tax, statutory deductions | Country-specific legal complexity; not required to replace Excel for salary *records* |
| Benefits administration | Separate domain; expands scope beyond compensation records |
| Employee / manager self-service | Persona is HR Manager only |
| SSO / IdP / multi-role RBAC | Demo JWT auth sufficient for local deploy |
| Multi-tenant organizations | Single ACME org |
| NLP “ask anything” Q&A | Analytics dashboard chosen instead |
| Mobile apps | Web UI only |
| Legal audit export systems | Structured server logs only; no compliance archive |

## Non-Functional Requirements

- **Performance:** p95 employee list response under 500 ms at 10k rows (paginated)
- **Pagination:** Required on all list endpoints; max page size 100
- **Security:** JWT on protected routes; CORS allow-list only; PII (name, email) masked in logs; passwords hashed (bcrypt); never log JWT or credentials
- **Data integrity:** Salary amounts stored as integers in minor currency units (cents)
- **Observability:** Structured JSON logs with correlation id; generic 4xx/5xx to clients

## Success Criteria

`docker compose up --build` → open UI → login as demo HR → browse/filter 10k employees → open detail and add a salary change → analytics dashboard shows populated aggregates.
 