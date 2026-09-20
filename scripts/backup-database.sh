#!/bin/bash

# Poker Stats - Database Backup Script
# This script creates PostgreSQL backups with rotation policy

set -e  # Exit on error
set -u  # Exit on undefined variable
set -o pipefail  # Fail if any command in a pipeline fails (e.g. pg_dump | gzip)

# ============================================
# Configuration
# ============================================

# Resolve paths relative to this script, so the script works from any directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Load environment variables
ENV_FILE="${ENV_FILE:-$PROJECT_ROOT/.env.production}"
if [ -f "$ENV_FILE" ]; then
    source "$ENV_FILE"
else
    echo "Error: environment file not found: $ENV_FILE" >&2
    echo "Copy .env.production.template to .env.production in $PROJECT_ROOT," >&2
    echo "or point ENV_FILE at your environment file." >&2
    exit 1
fi

# Backup configuration
BACKUP_ROOT="${BACKUP_PATH:-/mnt/pokerstats-data/backups}"
BACKUP_DIR="${BACKUP_ROOT}/postgres"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-30}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/pokerstats_${TIMESTAMP}.sql.gz"

# Database configuration
DB_CONTAINER="pokerstats-postgres-prod"
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

check_dependencies() {
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed"
        exit 1
    fi
}

check_container() {
    if ! docker ps | grep -q "$DB_CONTAINER"; then
        error "Database container '$DB_CONTAINER' is not running"
        exit 1
    fi
}

permission_hint() {
    local user_name group_name
    user_name="$(id -un)"
    group_name="$(id -gn)"

    cat >&2 <<HINT

The backup directory lives outside your home directory, so '$user_name' needs
to own it. Run this once on the host (the storage device must be mounted first):

    sudo mkdir -p "$BACKUP_DIR"
    sudo chown -R ${user_name}:${group_name} "$BACKUP_ROOT"

Alternatively point the backups at a directory you already own by setting
BACKUP_PATH in $ENV_FILE, for example:

    BACKUP_PATH=$HOME/pokerstats-data/backups
HINT
}

create_backup_dir() {
    if [ -d "$BACKUP_DIR" ]; then
        if [ ! -w "$BACKUP_DIR" ]; then
            error "Backup directory is not writable by $(id -un): $BACKUP_DIR"
            permission_hint
            exit 1
        fi
        return 0
    fi

    log "Creating backup directory: $BACKUP_DIR"
    if ! mkdir -p "$BACKUP_DIR" 2>/dev/null; then
        error "Cannot create backup directory: $BACKUP_DIR"
        permission_hint
        exit 1
    fi
}

create_backup() {
    log "Starting database backup..."
    log "Database: $DB_NAME"
    log "Backup file: $BACKUP_FILE"
    
    # Create backup using pg_dump
    # Note: no --compress here - pg_dump would gzip its plain-text output and
    # the extra gzip below would make the file double-compressed.
    if ! docker exec "$DB_CONTAINER" pg_dump \
        -U "$DB_USER" \
        -d "$DB_NAME" \
        --clean \
        --if-exists \
        --create \
        | gzip -9 > "$BACKUP_FILE"; then
        error "Backup failed"
        rm -f "$BACKUP_FILE"
        exit 1
    fi

    BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    log "Backup completed successfully"
    log "Backup size: $BACKUP_SIZE"
}

verify_backup() {
    log "Verifying backup integrity..."
    
    if gzip -t "$BACKUP_FILE" 2>/dev/null; then
        log "Backup verification successful"
    else
        error "Backup verification failed - file may be corrupted"
        exit 1
    fi
}

rotate_backups() {
    log "Rotating old backups (keeping last $RETENTION_DAYS days)..."
    
    # Find and delete backups older than retention period
    find "$BACKUP_DIR" -name "pokerstats_*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete
    
    REMAINING_BACKUPS=$(find "$BACKUP_DIR" -name "pokerstats_*.sql.gz" -type f | wc -l)
    log "Remaining backups: $REMAINING_BACKUPS"
}

send_notification() {
    local status=$1
    local message=$2
    
    # Log to system log (not available on every host - never fail the backup here)
    command -v logger >/dev/null 2>&1 && logger -t pokerstats-backup "$message" || true
    
    # TODO: Send email notification if configured
    # if [ -n "${SMTP_HOST:-}" ]; then
    #     echo "$message" | mail -s "Poker Stats Backup: $status" admin@pokerstats.local
    # fi
}

# ============================================
# Main Execution
# ============================================

main() {
    log "===== Poker Stats Database Backup Started ====="
    
    # Pre-flight checks
    check_dependencies
    check_container
    create_backup_dir
    
    # Create backup
    if create_backup && verify_backup; then
        rotate_backups
        send_notification "SUCCESS" "Database backup completed successfully: $BACKUP_FILE"
        log "===== Backup Process Completed Successfully ====="
        exit 0
    else
        send_notification "FAILED" "Database backup failed"
        error "===== Backup Process Failed ====="
        exit 1
    fi
}

# Run main function
main
