#!/bin/bash

# Poker Stats - Demo Data Seeding Script
# This script creates sample data for demonstration and testing

set -e  # Exit on error
set -u  # Exit on undefined variable

# ============================================
# Configuration
# ============================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DB_CONTAINER="pokerstats-postgres-prod"

# Load environment variables
if [ -f "$PROJECT_ROOT/.env.production" ]; then
    source "$PROJECT_ROOT/.env.production"
fi

DB_NAME="${POSTGRES_DB:-pokerstats}"
DB_USER="${POSTGRES_USER:-pokerstats}"

# ============================================
# Functions
# ============================================

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

error() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1" >&2
}

confirm_seeding() {
    echo ""
    echo "⚠️  This will create demo data in the database."
    echo "This should only be used for testing/demo purposes."
    echo ""
    read -p "Continue? (yes/no): " -r
    if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        log "Demo data seeding cancelled"
        exit 0
    fi
}

seed_demo_data() {
    log "Seeding demo data..."
    
    docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" <<'EOF'
-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create demo users
INSERT INTO users (id, email, password, name, role, created_at, updated_at)
VALUES 
    (uuid_generate_v4(), 'admin@demo.local', '$2a$10$dummyHashForDemo', 'Demo Admin', 'ADMIN', NOW(), NOW()),
    (uuid_generate_v4(), 'player1@demo.local', '$2a$10$dummyHashForDemo', 'John Doe', 'CASUAL_PLAYER', NOW(), NOW()),
    (uuid_generate_v4(), 'player2@demo.local', '$2a$10$dummyHashForDemo', 'Jane Smith', 'CASUAL_PLAYER', NOW(), NOW()),
    (uuid_generate_v4(), 'player3@demo.local', '$2a$10$dummyHashForDemo', 'Bob Johnson', 'CASUAL_PLAYER', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Create demo players
INSERT INTO players (id, name, user_id, active, created_at, updated_at)
SELECT 
    uuid_generate_v4(),
    name,
    id,
    true,
    created_at,
    updated_at
FROM users
WHERE email LIKE '%@demo.local'
ON CONFLICT (name) DO NOTHING;

-- Add a few non-user players
INSERT INTO players (id, name, user_id, active, created_at, updated_at)
VALUES 
    (uuid_generate_v4(), 'Mike Wilson', NULL, true, NOW(), NOW()),
    (uuid_generate_v4(), 'Sarah Davis', NULL, true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Create demo game sessions
DO $$
DECLARE
    session_id UUID;
    player_ids UUID[];
    i INTEGER;
BEGIN
    -- Get player IDs
    SELECT ARRAY_AGG(id) INTO player_ids FROM players WHERE active = true LIMIT 6;
    
    -- Create 10 demo sessions over the past month
    FOR i IN 1..10 LOOP
        session_id := uuid_generate_v4();
        
        INSERT INTO game_sessions (
            id, 
            start_time, 
            end_time, 
            location, 
            min_buy_in_amount_in_cents, 
            game_type,
            notes,
            created_at,
            updated_at,
            deleted
        ) VALUES (
            session_id,
            NOW() - (i || ' days')::INTERVAL,
            NOW() - (i || ' days')::INTERVAL + INTERVAL '4 hours',
            CASE (i % 3)
                WHEN 0 THEN 'Johns House'
                WHEN 1 THEN 'Club Downtown'
                ELSE 'Mikes Garage'
            END,
            5000, -- 50 PLN
            'TEXAS_HOLDEM',
            'Demo session ' || i,
            NOW(),
            NOW(),
            false
        );
        
        -- Add random results for 4-6 players per session
        INSERT INTO session_results (
            id,
            session_id,
            player_id,
            buy_in_amount_in_cents,
            cash_out_amount_in_cents,
            notes,
            created_at,
            updated_at
        )
        SELECT 
            uuid_generate_v4(),
            session_id,
            player_ids[gs],
            (50 + (RANDOM() * 50)::INTEGER) * 100, -- 50-100 PLN buy-in
            (30 + (RANDOM() * 120)::INTEGER) * 100, -- 30-150 PLN cash-out
            NULL,
            NOW(),
            NOW()
        FROM generate_series(1, 4 + (RANDOM() * 2)::INTEGER) gs;
        
    END LOOP;
END $$;

-- Display summary
SELECT 
    'Users' as entity,
    COUNT(*) as count
FROM users WHERE email LIKE '%@demo.local'
UNION ALL
SELECT 
    'Players',
    COUNT(*)
FROM players
UNION ALL
SELECT 
    'Sessions',
    COUNT(*)
FROM game_sessions WHERE deleted = false
UNION ALL
SELECT 
    'Results',
    COUNT(*)
FROM session_results;

EOF
    
    if [ $? -eq 0 ]; then
        log "Demo data seeded successfully!"
        echo ""
        echo "============================================"
        echo "Demo Users Created"
        echo "============================================"
        echo "Admin:   admin@demo.local / demo123"
        echo "Player1: player1@demo.local / demo123"
        echo "Player2: player2@demo.local / demo123"
        echo "Player3: player3@demo.local / demo123"
        echo ""
        echo "⚠️  Note: These are dummy passwords. Please log in and change them."
        echo "============================================"
    else
        error "Failed to seed demo data"
        exit 1
    fi
}

cleanup_demo_data() {
    log "Cleaning up demo data..."
    
    docker exec -i "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" <<'EOF'
-- Delete demo session results
DELETE FROM session_results WHERE session_id IN (
    SELECT id FROM game_sessions WHERE notes LIKE 'Demo session%'
);

-- Delete demo sessions
DELETE FROM game_sessions WHERE notes LIKE 'Demo session%';

-- Delete demo players
DELETE FROM players WHERE name IN ('Mike Wilson', 'Sarah Davis');

-- Delete demo player-users
DELETE FROM players WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@demo.local'
);

-- Delete demo users
DELETE FROM users WHERE email LIKE '%@demo.local';

SELECT 'Demo data cleaned up successfully' as status;
EOF
    
    log "Demo data cleanup completed"
}

check_prerequisites() {
    if ! docker ps | grep -q "$DB_CONTAINER"; then
        error "Database container '$DB_CONTAINER' is not running"
        exit 1
    fi
}

# ============================================
# Main Execution
# ============================================

main() {
    log "===== Demo Data Management ====="
    echo ""
    
    check_prerequisites
    
    echo "Select action:"
    echo "1) Seed demo data"
    echo "2) Clean up demo data"
    echo "3) Cancel"
    read -p "Enter choice (1-3): " ACTION
    
    case $ACTION in
        1)
            confirm_seeding
            seed_demo_data
            ;;
        2)
            cleanup_demo_data
            ;;
        3)
            log "Operation cancelled"
            exit 0
            ;;
        *)
            error "Invalid choice"
            exit 1
            ;;
    esac
}

# Run main function
main
