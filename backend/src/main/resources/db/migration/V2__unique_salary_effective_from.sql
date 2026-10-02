-- Enforce one salary change per employee per effective date.
-- Tie-break for any pre-existing duplicates: keep newest created_at, then highest id.

DELETE FROM salary_record
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY employee_id, effective_from
                   ORDER BY created_at DESC, id DESC
               ) AS rn
        FROM salary_record
    ) ranked
    WHERE rn > 1
);

DROP INDEX IF EXISTS idx_salary_employee_effective;

ALTER TABLE salary_record
    ADD CONSTRAINT uq_salary_employee_effective UNIQUE (employee_id, effective_from);
