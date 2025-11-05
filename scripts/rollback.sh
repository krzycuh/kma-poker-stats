#!/bin/bash

# Poker Stats - Rollback Script
# This script rolls back the application to a previous version

set -e  # Exit on error
set -u  # Exit on undefined variable

# ============================================
# Configuration
# ============================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILE="docker-compose.prod.yml"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================
# Functions
# ============================================

log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} ✓ $1"
}

warning() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} ⚠ $1"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} ✗ $1" >&2
}

print_header() {
    echo ""
    echo "=================================================="
    echo "  Poker Stats - Application Rollback"
    echo "=================================================="
    echo ""
}

list_available_versions() {
    log "Available Docker image versions:"
    echo ""
    
    # List backend versions
    echo "Backend versions:"
    docker images pokerstats-backend --format "{{.Tag}}\t{{.CreatedAt}}\t{{.Size}}" | head -5
    echo ""
    
    # List frontend versions
    echo "Frontend versions:"
    docker images pokerstats-frontend --format "{{.Tag}}\t{{.CreatedAt}}\t{{.Size}}" | head -5
    echo ""
}

list_git_versions() {
    log "Recent Git commits:"
    echo ""
    cd "$PROJECT_ROOT"
    git log --oneline -10
    echo ""
}

select_rollback_method() {
    echo "Select rollback method:"
    echo "1) Roll back to previous Git commit"
    echo "2) Roll back to previous Docker image"
    echo "3) Restore from database backup"
    echo "4) Full rollback (Git + Database)"
    echo "q) Quit"
    echo ""
    read -p "Enter selection: " -r method
    
    case $method in
        1) rollback_git ;;
        2) rollback_docker_image ;;
        3) rollback_database ;;
        4) rollback_full ;;
        q) exit 0 ;;
        *) error "Invalid selection"; exit 1 ;;
    esac
}

rollback_git() {
    log "Rolling back to previous Git commit..."
    
    cd "$PROJECT_ROOT"
    
    # Show current commit
    log "Current commit: $(git rev-parse --short HEAD)"
    
    # Ask for target commit
    read -p "Enter commit hash to rollback to (or 'HEAD~1' for previous): " -r target
    
    # Confirm
    warning "This will reset to commit: $target"
    read -p "Continue? (yes/no): " -r
    if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        log "Rollback cancelled"
        exit 0
    fi
    
    # Create backup branch
    local backup_branch="backup-$(date +%Y%m%d-%H%M%S)"
    git branch "$backup_branch"
    log "Created backup branch: $backup_branch"
    
    # Reset to target
    git reset --hard "$target"
    
    success "Code rolled back to $target"
    
    # Rebuild and redeploy
    log "Rebuilding application..."
    bash "$SCRIPT_DIR/deploy.sh"
}

rollback_docker_image() {
    log "Rolling back to previous Docker image version..."
    
    # List available images
    list_available_versions
    
    # Ask for version
    read -p "Enter version tag to rollback to: " -r version
    
    # Check if images exist
    if ! docker images pokerstats-backend:$version | grep -q "$version"; then
        error "Backend image version $version not found"
        exit 1
    fi
    
    if ! docker images pokerstats-frontend:$version | grep -q "$version"; then
        error "Frontend image version $version not found"
        exit 1
    fi
    
    # Update version in environment
    export VERSION=$version
    
    # Redeploy with old images
    log "Deploying version $version..."
    cd "$PROJECT_ROOT"
    docker-compose -f $COMPOSE_FILE down
    docker-compose -f $COMPOSE_FILE up -d
    
    success "Rolled back to version $version"
}

rollback_database() {
    log "Rolling back database..."
    
    # Run restore script
    bash "$SCRIPT_DIR/restore-database.sh"
}

rollback_full() {
    log "Performing full rollback (code + database)..."
    
    warning "This will restore both code and database to previous state"
    read -p "Continue? (yes/no): " -r
    if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        log "Rollback cancelled"
        exit 0
    fi
    
    # Rollback database first
    rollback_database
    
    # Then rollback code
    rollback_git
}

verify_rollback() {
    log "Verifying rollback..."
    
    # Check if services are healthy
    sleep 10
    
    if curl -sf http://localhost:8080/actuator/health > /dev/null; then
        success "Backend is healthy after rollback"
    else
        error "Backend health check failed after rollback"
        return 1
    fi
    
    success "Rollback verification successful"
}

# ============================================
# Main Execution
# ============================================

main() {
    print_header
    
    # Check if running in project root
    if [ ! -f "$PROJECT_ROOT/$COMPOSE_FILE" ]; then
        error "docker-compose.prod.yml not found. Are you in the project root?"
        exit 1
    fi
    
    # Show current status
    log "Current deployment status:"
    docker-compose -f $COMPOSE_FILE ps
    echo ""
    
    # Select rollback method
    select_rollback_method
    
    # Verify
    verify_rollback
    
    success "Rollback completed successfully"
}

# Run main function
main
