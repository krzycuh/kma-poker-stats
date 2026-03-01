-- Session expenses table: tracks individual expense items (snacks, alcohol, etc.) for a session
CREATE TABLE session_expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
    payer_player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    amount_cents BIGINT NOT NULL CHECK (amount_cents > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_session_expenses_session_id ON session_expenses(session_id);
CREATE INDEX idx_session_expenses_payer ON session_expenses(payer_player_id);

-- Reuse existing updated_at trigger function
CREATE TRIGGER update_session_expenses_updated_at BEFORE UPDATE ON session_expenses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
