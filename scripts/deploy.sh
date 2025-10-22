#!/bin/bash

# Poker Stats - Production Deployment Script
# This script deploys the application to production

set -e  # Exit on error
set -u  # Exit on undefined variable

# ============================================
# Configuration
# ============================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.production"

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
    echo "  Poker Stats - Production Deployment"
    echo "=================================================="
    echo ""
}

check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check if running as root or with sudo
    if [ "$EUID" -eq 0 ]; then
        warning "Running as root. Consider using a non-root user with sudo."
    fi
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    # Check if .env.production exists
    if [ ! -f "$PROJECT_ROOT/$ENV_FILE" ]; then
        error "Environment file $ENV_FILE not found."
        echo "Please copy .env.production.template to .env.production and configure it."
        exit 1
    fi
    
    # Check if required directories exist
    source "$PROJECT_ROOT/$ENV_FILE"
    if [ ! -d "${DATA_PATH:-/mnt/pokerstats-data}" ]; then
        warning "Data path ${DATA_PATH:-/mnt/pokerstats-data} does not exist. Creating it..."
        mkdir -p "${DATA_PATH:-/mnt/pokerstats-data}"/{postgres,redis,prometheus,grafana,backups}
    fi
    
    success "All prerequisites met"
}

validate_environment() {
    log "Validating environment configuration..."
    
    source "$PROJECT_ROOT/$ENV_FILE"
    
    # Check for default passwords
    if [[ "$POSTGRES_PASSWORD" == *"CHANGE_ME"* ]]; then
        error "PostgreSQL password has not been changed from default"
        exit 1
    fi
    
    if [[ "$JWT_SECRET" == *"CHANGE_ME"* ]]; then
        error "JWT secret has not been changed from default"
        exit 1
    fi
    
    if [[ "$REDIS_PASSWORD" == *"CHANGE_ME"* ]]; then
        error "Redis password has not been changed from default"
        exit 1
    fi
    
    success "Environment configuration is valid"
}

create_backup() {
    log "Creating pre-deployment backup..."
    
    # Check if database container is running
    if docker ps | grep -q "pokerstats-postgres-prod"; then
        bash "$SCRIPT_DIR/backup-database.sh" || {
            warning "Backup failed, but continuing with deployment"
            return 0
        }
        success "Backup created successfully"
    else
        log "No existing database found, skipping backup"
    fi
}

pull_latest_code() {
    log "Pulling latest code from repository..."
    
    cd "$PROJECT_ROOT"
    
    if [ -d ".git" ]; then
        # Save current branch
        CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
        
        # Fetch latest changes
        git fetch origin
        
        # Show what will be updated
        log "Current branch: $CURRENT_BRANCH"
        log "Changes to be pulled:"
        git log HEAD..origin/$CURRENT_BRANCH --oneline
        
        # Pull changes
        git pull origin $CURRENT_BRANCH
        
        success "Code updated successfully"
    else
        warning "Not a git repository, skipping code update"
    fi
}

build_images() {
    log "Building Docker images..."
    
    cd "$PROJECT_ROOT"
    
    # Set build arguments
    export BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
    export VCS_REF=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
    export VERSION=${VERSION:-latest}
    
    # Build images
    docker-compose -f $COMPOSE_FILE build --no-cache --pull
    
    success "Images built successfully"
}

run_database_migrations() {
    log "Running database migrations..."
    
    # Start only the database
    docker-compose -f $COMPOSE_FILE up -d postgres
    
    # Wait for database to be ready
    log "Waiting for database to be ready..."
    sleep 10
    
    # Run migrations via backend
    docker-compose -f $COMPOSE_FILE run --rm backend /bin/sh -c "
        echo 'Running Flyway migrations...'
        ./gradlew flywayMigrate -i
    " || {
        error "Database migrations failed"
        exit 1
    }
    
    success "Database migrations completed"
}

deploy_services() {
    log "Deploying services..."
    
    cd "$PROJECT_ROOT"
    
    # Stop existing services (if any)
    if docker-compose -f $COMPOSE_FILE ps -q | grep -q .; then
        log "Stopping existing services..."
        docker-compose -f $COMPOSE_FILE stop
    fi
    
    # Start services
    log "Starting services..."
    docker-compose -f $COMPOSE_FILE up -d
    
    success "Services started"
}

wait_for_health() {
    log "Waiting for services to be healthy..."
    
    local max_attempts=30
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if docker-compose -f $COMPOSE_FILE ps | grep -q "unhealthy"; then
            attempt=$((attempt + 1))
            echo -n "."
            sleep 2
        else
            echo ""
            success "All services are healthy"
            return 0
        fi
    done
    
    error "Services did not become healthy within expected time"
    docker-compose -f $COMPOSE_FILE ps
    return 1
}

verify_deployment() {
    log "Verifying deployment..."
    
    # Check if all containers are running
    local expected_containers=("postgres" "redis" "backend" "frontend" "nginx" "prometheus" "grafana")
    local all_running=true
    
    for container in "${expected_containers[@]}"; do
        if ! docker ps | grep -q "pokerstats-$container"; then
            warning "Container pokerstats-$container is not running"
            all_running=false
        fi
    done
    
    if [ "$all_running" = false ]; then
        error "Not all containers are running"
        docker-compose -f $COMPOSE_FILE ps
        return 1
    fi
    
    # Check backend health endpoint
    log "Checking backend health..."
    local health_url="http://localhost:8080/actuator/health"
    if curl -sf "$health_url" > /dev/null; then
        success "Backend is healthy"
    else
        error "Backend health check failed"
        return 1
    fi
    
    success "Deployment verification successful"
}

cleanup() {
    log "Cleaning up old images..."
    docker image prune -f
    success "Cleanup completed"
}

print_summary() {
    echo ""
    echo "=================================================="
    echo "  Deployment Summary"
    echo "=================================================="
    echo ""
    docker-compose -f $COMPOSE_FILE ps
    echo ""
    echo "Application URLs:"
    echo "  - Frontend: https://$(hostname)"
    echo "  - API: https://$(hostname)/api"
    echo "  - Grafana: http://localhost:3000"
    echo "  - Prometheus: http://localhost:9090"
    echo ""
    echo "Logs:"
    echo "  docker-compose -f $COMPOSE_FILE logs -f [service]"
    echo ""
    success "Deployment completed successfully!"
}

rollback() {
    error "Deployment failed! Rolling back..."
    
    # Stop new containers
    docker-compose -f $COMPOSE_FILE down
    
    # Restore from backup if it exists
    if [ -f "$SCRIPT_DIR/backup-database.sh" ]; then
        warning "Consider running restore-database.sh to restore from backup"
    fi
    
    error "Rollback completed. Please check logs and fix issues before retrying."
    exit 1
}

# ============================================
# Main Deployment Flow
# ============================================

main() {
    print_header
    
    # Set trap for errors
    trap rollback ERR
    
    # Pre-deployment checks
    check_prerequisites
    validate_environment
    
    # Ask for confirmation
    echo ""
    warning "This will deploy Poker Stats to production."
    read -p "Continue? (yes/no): " -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        log "Deployment cancelled"
        exit 0
    fi
    
    # Execute deployment steps
    create_backup
    pull_latest_code
    build_images
    run_database_migrations
    deploy_services
    wait_for_health
    verify_deployment
    cleanup
    print_summary
}

# Run main function
cd "$PROJECT_ROOT"
main
