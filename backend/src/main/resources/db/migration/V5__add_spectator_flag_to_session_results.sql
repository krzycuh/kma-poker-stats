-- Add is_spectator column to session_results table
ALTER TABLE session_results
ADD COLUMN is_spectator BOOLEAN NOT NULL DEFAULT false;

-- Add index on is_spectator for efficient filtering in queries
CREATE INDEX idx_session_results_is_spectator ON session_results(is_spectator);

-- Update the placement backfill logic to exclude spectators
-- This will recalculate placements for all existing sessions, excluding any future spectators
-- For existing data, all records have is_spectator = false, so this won't change anything now
WITH ranked_results AS (
    SELECT
        id,
        session_id,
        cash_out_cents - buy_in_cents AS profit,
        is_spectator,
        DENSE_RANK() OVER (
            PARTITION BY session_id
            ORDER BY (cash_out_cents - buy_in_cents) DESC
        ) AS calculated_placement
    FROM session_results
    WHERE is_spectator = false  -- Only rank non-spectators
)
UPDATE session_results
SET placement = CASE
    WHEN session_results.is_spectator = true THEN NULL  -- Spectators get NULL placement
    ELSE ranked_results.calculated_placement
END
FROM ranked_results
WHERE session_results.id = ranked_results.id;

-- Set spectator results to NULL placement
UPDATE session_results
SET placement = NULL
WHERE is_spectator = true;
