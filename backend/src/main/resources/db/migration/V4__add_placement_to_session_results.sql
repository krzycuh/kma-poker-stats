-- Add placement column to session_results table
ALTER TABLE session_results
ADD COLUMN placement INTEGER;

-- Add check constraint to ensure placement is positive
ALTER TABLE session_results
ADD CONSTRAINT session_results_placement_positive CHECK (placement IS NULL OR placement >= 1);

-- Backfill placement data for existing sessions
-- Calculate placement based on profit (cash_out - buy_in) in descending order
-- Players with same profit get the same placement
WITH ranked_results AS (
    SELECT
        id,
        session_id,
        cash_out_cents - buy_in_cents AS profit,
        DENSE_RANK() OVER (
            PARTITION BY session_id
            ORDER BY (cash_out_cents - buy_in_cents) DESC
        ) AS calculated_placement
    FROM session_results
)
UPDATE session_results
SET placement = ranked_results.calculated_placement
FROM ranked_results
WHERE session_results.id = ranked_results.id;

-- Create index on placement for efficient queries
CREATE INDEX idx_session_results_placement ON session_results(placement);
