-- Ensure a user account can be linked to at most one player
CREATE UNIQUE INDEX IF NOT EXISTS ux_players_user_id_not_null
    ON players(user_id)
    WHERE user_id IS NOT NULL;
