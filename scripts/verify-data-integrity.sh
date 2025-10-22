#!/bin/bash

# Poker Stats - Data Integrity Verification Script
# This script verifies the integrity of the database

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

success() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] ✓ $1"
}

error() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] ✗ $1" >&2
}

warning() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] ⚠ $1"
}

check_database_connection() {
    log "Checking database connection..."
    
    if ! docker exec "$DB_CONTAINER" pg_isready -U "$DB_USER" -d "$DB_NAME" &>/dev/null; then
        error "Cannot connect to database"
        return 1
    fi
    
    success "Database connection OK"
    return 0
}

check_required_tables() {
    log "Checking required tables exist..."
    
    local required_tables=("users" "players" "game_sessions" "session_results")
    local missing_tables=()
    
    for table in "${required_tables[@]}"; do
        if ! docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" \
            -c "SELECT 1 FROM $table LIMIT 1;" &>/dev/null; then
            missing_tables+=("$table")
        fi
    done
    
    if [ ${#missing_tables[@]} -gt 0 ]; then
        error "Missing tables: ${missing_tables[*]}"
        return 1
    fi
    
    success "All required tables exist"
    return 0
}

check_foreign_key_constraints() {
    log "Checking foreign key integrity..."
    
    local issues=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t <<'EOF'
-- Check for orphaned players (user_id references non-existent user)
SELECT 'Orphaned players (invalid user_id): ' || COUNT(*)
FROM players p
WHERE p.user_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.id = p.user_id);

-- Check for orphaned session results (invalid player_id)
SELECT 'Orphaned results (invalid player_id): ' || COUNT(*)
FROM session_results sr
WHERE NOT EXISTS (SELECT 1 FROM players p WHERE p.id = sr.player_id);

-- Check for orphaned session results (invalid session_id)
SELECT 'Orphaned results (invalid session_id): ' || COUNT(*)
FROM session_results sr
WHERE NOT EXISTS (SELECT 1 FROM game_sessions gs WHERE gs.id = sr.session_id);
EOF
    )
    
    echo "$issues"
    
    if echo "$issues" | grep -vq ": 0"; then
        warning "Found foreign key integrity issues"
        return 1
    fi
    
    success "Foreign key integrity OK"
    return 0
}

check_data_consistency() {
    log "Checking data consistency..."
    
    local issues=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t <<'EOF'
-- Check for sessions with less than 2 players
SELECT 'Sessions with < 2 players: ' || COUNT(*)
FROM (
    SELECT s.id, COUNT(r.id) as player_count
    FROM game_sessions s
    LEFT JOIN session_results r ON s.id = r.session_id
    WHERE s.deleted = false
    GROUP BY s.id
    HAVING COUNT(r.id) < 2
) sub;

-- Check for negative buy-ins
SELECT 'Negative buy-ins: ' || COUNT(*)
FROM session_results
WHERE buy_in_amount_in_cents < 0;

-- Check for negative cash-outs
SELECT 'Negative cash-outs: ' || COUNT(*)
FROM session_results
WHERE cash_out_amount_in_cents < 0;

-- Check for duplicate user emails
SELECT 'Duplicate emails: ' || COUNT(*) - COUNT(DISTINCT email)
FROM users;

-- Check for players with duplicate names
SELECT 'Duplicate player names: ' || COUNT(*) - COUNT(DISTINCT name)
FROM players WHERE active = true;
EOF
    )
    
    echo "$issues"
    
    if echo "$issues" | grep -vq ": 0"; then
        warning "Found data consistency issues"
        return 1
    fi
    
    success "Data consistency OK"
    return 0
}

check_session_balances() {
    log "Checking session balance discrepancies..."
    
    local unbalanced=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t <<'EOF'
SELECT COUNT(*)
FROM (
    SELECT 
        s.id,
        ABS(SUM(r.buy_in_amount_in_cents) - SUM(r.cash_out_amount_in_cents)) as discrepancy
    FROM game_sessions s
    JOIN session_results r ON s.id = r.session_id
    WHERE s.deleted = false
    GROUP BY s.id
    HAVING ABS(SUM(r.buy_in_amount_in_cents) - SUM(r.cash_out_amount_in_cents)) > 100
) sub;
EOF
    )
    
    unbalanced=$(echo "$unbalanced" | xargs) # Trim whitespace
    
    if [ "$unbalanced" -gt 0 ]; then
        warning "Found $unbalanced sessions with balance discrepancies > 1 PLN"
        log "Note: Small discrepancies are acceptable (tips, dealer fees, etc.)"
    else
        success "All sessions are balanced"
    fi
}

generate_statistics() {
    log "Generating database statistics..."
    
    docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" <<'EOF'
\echo ''
\echo '=========================================='
\echo 'Database Statistics'
\echo '=========================================='
\echo ''

SELECT 'Total Users' as metric, COUNT(*)::text as value FROM users
UNION ALL
SELECT 'Admin Users', COUNT(*)::text FROM users WHERE role = 'ADMIN'
UNION ALL
SELECT 'Casual Players', COUNT(*)::text FROM users WHERE role = 'CASUAL_PLAYER'
UNION ALL
SELECT 'Total Players', COUNT(*)::text FROM players
UNION ALL
SELECT 'Active Players', COUNT(*)::text FROM players WHERE active = true
UNION ALL
SELECT 'Total Sessions', COUNT(*)::text FROM game_sessions WHERE deleted = false
UNION ALL
SELECT 'Total Results', COUNT(*)::text FROM session_results
UNION ALL
SELECT 'Avg Players/Session', ROUND(AVG(player_count), 1)::text
FROM (
    SELECT COUNT(r.id) as player_count
    FROM game_sessions s
    LEFT JOIN session_results r ON s.id = r.session_id
    WHERE s.deleted = false
    GROUP BY s.id
) sub;

\echo ''
\echo 'Recent Activity (Last 30 Days)'
SELECT 
    'Sessions Created' as metric,
    COUNT(*)::text as value
FROM game_sessions
WHERE created_at > NOW() - INTERVAL '30 days' AND deleted = false;

\echo ''
\echo 'Database Size'
SELECT 
    pg_size_pretty(pg_database_size(current_database())) as "Total Size";

\echo ''
EOF
}

check_indexes() {
    log "Checking database indexes..."
    
    docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" <<'EOF'
\echo 'Existing Indexes:'
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
EOF
    
    success "Index check completed"
}

run_vacuum_analyze() {
    log "Running VACUUM ANALYZE to update statistics..."
    
    docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" \
        -c "VACUUM ANALYZE;" &>/dev/null
    
    success "Database statistics updated"
}

# ============================================
# Main Execution
# ============================================

main() {
    echo "=========================================="
    echo "  Poker Stats - Data Integrity Check"
    echo "=========================================="
    echo ""
    
    local exit_code=0
    
    # Run all checks
    check_database_connection || exit_code=1
    check_required_tables || exit_code=1
    check_foreign_key_constraints || exit_code=1
    check_data_consistency || exit_code=1
    check_session_balances || true  # Don't fail on balance warnings
    check_indexes || true
    
    # Generate statistics
    echo ""
    generate_statistics
    
    # Optimize database
    echo ""
    run_vacuum_analyze
    
    # Summary
    echo ""
    echo "=========================================="
    if [ $exit_code -eq 0 ]; then
        success "Data integrity verification PASSED"
    else
        error "Data integrity verification FAILED"
        echo "Please review the issues above and fix them"
    fi
    echo "=========================================="
    
    exit $exit_code
}

# Check prerequisites
if ! docker ps | grep -q "$DB_CONTAINER"; then
    error "Database container '$DB_CONTAINER' is not running"
    exit 1
fi

# Run main function
main
