# ACME Salary Management — Performance

## Targets

| Metric | Target |
|--------|--------|
| Employee list p95 (10k rows, paginated) | &lt; 500 ms |
| Max page size | 100 |
| Seed of 10,000 employees | &lt; 60 s |
| Analytics queries | SQL aggregates only; no full entity load |

## Indexes

| Table | Index | Purpose |
|-------|-------|---------|
| `employee` | `(last_name, first_name)` | Name search |
| `employee` | `employee_number` (unique) | Default list sort / lookup |
| `employee` | `department` | Department filter |
| `employee` | `country_code` | Country filter |
| `employee` | `status` | Status filter |
| `salary_record` | `(employee_id, effective_from DESC)` | Current salary + history |

## Pagination

- All list endpoints require `page` (0-based) and `size`
- Reject `size` &gt; 100 at the validation boundary
- Prefer keyset/offset via Spring Data `Pageable`; default sort by employee number

## Analytics strategy

- Use SQL aggregates (`COUNT`, `SUM`, `AVG`, `GROUP BY`) against current-salary views or subqueries
- Never load 10k employees into application memory for dashboard metrics
- Salary distribution bands computed in SQL or via a single aggregated scan

## Seeding strategy

- Batch inserts in chunks of 500 (JDBC batch / `saveAll`)
- Deterministic generators (fixed seed) for repeatable demos
- Optional: create secondary indexes after bulk load when practical
- Gated by `SEED_ON_START=true`; skip when employee count already meets target

## Connection pool

- HikariCP pool size ~10 for Compose (single API replica)
- API waits on PostgreSQL healthcheck before accepting traffic

## Frontend

- Server-side pagination for the employee table
- Virtual scroll optional if the table feels heavy; not required for v1
- Analytics charts consume pre-aggregated API payloads only
 