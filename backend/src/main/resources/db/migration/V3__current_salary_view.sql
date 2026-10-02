-- Deterministic current salary: latest effective_from, then newest created_at, then highest id.
CREATE VIEW current_salary AS
SELECT id,
       employee_id,
       amount_minor,
       currency_code,
       effective_from,
       change_reason,
       created_at
FROM (
    SELECT id,
           employee_id,
           amount_minor,
           currency_code,
           effective_from,
           change_reason,
           created_at,
           ROW_NUMBER() OVER (
               PARTITION BY employee_id
               ORDER BY effective_from DESC, created_at DESC, id DESC
           ) AS rn
    FROM salary_record
) ranked
WHERE rn = 1;
