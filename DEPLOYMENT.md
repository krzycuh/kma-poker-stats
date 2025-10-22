# Poker Stats - Deployment Guide

This guide covers deploying the Poker Stats application to a production environment, specifically optimized for Raspberry Pi deployment.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Production Environment Setup](#production-environment-setup)
- [Initial Deployment](#initial-deployment)
- [Ongoing Deployments](#ongoing-deployments)
- [Rollback Procedures](#rollback-procedures)
- [Monitoring and Maintenance](#monitoring-and-maintenance)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Hardware Requirements

**Raspberry Pi 4 (Recommended)**
- Model: Raspberry Pi 4 Model B
- RAM: Minimum 4GB, 8GB recommended
- Storage: External SSD (minimum 128GB recommended)
- Network: Ethernet connection preferred for stability

### Software Requirements

- **Operating System**: Raspberry Pi OS (64-bit) or Ubuntu Server 22.04 LTS
- **Docker**: Version 20.10 or later
- **Docker Compose**: Version 2.0 or later
- **Git**: For code management
- **OpenSSL**: For generating secrets

### Network Requirements

- Static IP address or DDNS
- Port 80 and 443 accessible (HTTP/HTTPS)
- Firewall configured appropriately
- (Optional) Domain name pointing to your server

---

## Production Environment Setup

### Step 1: Prepare the System

```bash
# Update system packages
sudo apt update && sudo apt upgrade -y

# Install required packages
sudo apt install -y git curl openssl

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add your user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Install Docker Compose
sudo apt install docker-compose-plugin

# Verify installations
docker --version
docker compose version
```

### Step 2: Setup External SSD (Recommended)

```bash
# Identify your SSD
lsblk

# Format SSD (if new) - CAUTION: This erases all data!
sudo mkfs.ext4 /dev/sda1

# Create mount point
sudo mkdir -p /mnt/pokerstats-data

# Mount the SSD
sudo mount /dev/sda1 /mnt/pokerstats-data

# Make mount permanent
echo "/dev/sda1 /mnt/pokerstats-data ext4 defaults,nofail 0 2" | sudo tee -a /etc/fstab

# Set ownership
sudo chown -R $USER:$USER /mnt/pokerstats-data

# Create required directories
mkdir -p /mnt/pokerstats-data/{postgres,redis,prometheus,grafana,backups}
```

### Step 3: Clone the Repository

```bash
# Choose installation directory
sudo mkdir -p /opt/pokerstats
sudo chown -R $USER:$USER /opt/pokerstats

# Clone repository
cd /opt/pokerstats
git clone <your-repo-url> .

# Checkout the desired branch (usually main)
git checkout main
```

### Step 4: Configure Environment Variables

```bash
# Copy template to production config
cp .env.production.template .env.production

# Generate secure passwords and secrets
echo "POSTGRES_PASSWORD=$(openssl rand -base64 32)" >> .env.production
echo "REDIS_PASSWORD=$(openssl rand -base64 32)" >> .env.production
echo "JWT_SECRET=$(openssl rand -base64 64)" >> .env.production
echo "GRAFANA_ADMIN_PASSWORD=$(openssl rand -base64 20)" >> .env.production

# Edit .env.production and configure remaining values
nano .env.production
```

**Important variables to configure:**
- `DATA_PATH`: Set to `/mnt/pokerstats-data`
- `CORS_ALLOWED_ORIGINS`: Your domain (e.g., `https://pokerstats.yourdomain.com`)
- `VITE_API_URL`: Your API URL
- Email settings (if using notifications)

### Step 5: Setup SSL Certificates

#### Option A: Let's Encrypt (Recommended)

```bash
# Install certbot
sudo apt install certbot

# Generate certificate
sudo certbot certonly --standalone -d pokerstats.yourdomain.com

# Copy certificates to nginx directory
sudo mkdir -p nginx/ssl
sudo cp /etc/letsencrypt/live/pokerstats.yourdomain.com/fullchain.pem nginx/ssl/
sudo cp /etc/letsencrypt/live/pokerstats.yourdomain.com/privkey.pem nginx/ssl/
sudo cp /etc/letsencrypt/live/pokerstats.yourdomain.com/chain.pem nginx/ssl/

# Set up auto-renewal
sudo crontab -e
# Add: 0 3 * * * certbot renew --quiet && cp /etc/letsencrypt/live/pokerstats.yourdomain.com/*.pem /opt/pokerstats/nginx/ssl/
```

#### Option B: Self-Signed Certificate (Development/Testing)

```bash
# Generate self-signed certificate
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/privkey.pem \
  -out nginx/ssl/fullchain.pem \
  -subj "/CN=pokerstats.local"

# Create chain file
cp nginx/ssl/fullchain.pem nginx/ssl/chain.pem
```

### Step 6: Configure Firewall

```bash
# Allow SSH
sudo ufw allow 22/tcp

# Allow HTTP and HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Enable firewall
sudo ufw enable

# Check status
sudo ufw status
```

---

## Initial Deployment

### Step 1: Build Images

```bash
cd /opt/pokerstats

# Build Docker images
export VERSION=1.0.0
docker-compose -f docker-compose.prod.yml build
```

### Step 2: Run Database Migrations

```bash
# Start only the database first
docker-compose -f docker-compose.prod.yml up -d postgres

# Wait for database to be ready
sleep 10

# Migrations will run automatically when backend starts
docker-compose -f docker-compose.prod.yml up -d backend
```

### Step 3: Create Admin User

```bash
# Run the admin user creation script
bash scripts/create-admin-user.sh

# Save the credentials securely!
```

### Step 4: Deploy All Services

```bash
# Deploy using the deployment script
bash scripts/deploy.sh
```

The deployment script will:
1. Check prerequisites
2. Validate environment configuration
3. Create a backup (if database exists)
4. Pull latest code
5. Build Docker images
6. Run database migrations
7. Deploy all services
8. Wait for health checks
9. Verify deployment

### Step 5: Verify Deployment

```bash
# Check all containers are running
docker-compose -f docker-compose.prod.yml ps

# Check backend health
curl http://localhost:8080/actuator/health

# Check frontend (in browser)
https://pokerstats.yourdomain.com

# Check Grafana
http://localhost:3000
```

---

## Ongoing Deployments

### Automated Deployment via GitHub Actions

When you push a release tag, GitHub Actions will automatically:
1. Build Docker images
2. Push to container registry
3. SSH to production server
4. Create backup
5. Deploy new version
6. Verify deployment
7. Rollback on failure

### Manual Deployment

```bash
# Navigate to project directory
cd /opt/pokerstats

# Run deployment script
bash scripts/deploy.sh
```

### Zero-Downtime Deployment

The deployment script handles this automatically by:
1. Building new images
2. Running migrations while old version is still up
3. Starting new containers
4. Nginx routes traffic once containers are healthy
5. Old containers are stopped

---

## Rollback Procedures

### Automatic Rollback

If deployment fails, GitHub Actions will automatically rollback.

### Manual Rollback

```bash
# Run rollback script
bash scripts/rollback.sh

# Select rollback method:
# 1) Roll back to previous Git commit
# 2) Roll back to previous Docker image
# 3) Restore from database backup
# 4) Full rollback (Git + Database)
```

### Database Restore

```bash
# List available backups
bash scripts/restore-database.sh

# Follow prompts to select backup and restore
```

---

## Monitoring and Maintenance

### Access Monitoring Dashboards

**Grafana**: `http://your-server:3000`
- Username: `admin`
- Password: (from `.env.production`)

**Prometheus**: `http://your-server:9090`

### Daily Operations

**View Logs:**
```bash
# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker-compose -f docker-compose.prod.yml logs -f backend
```

**Restart Services:**
```bash
# Restart single service
docker-compose -f docker-compose.prod.yml restart backend

# Restart all services
docker-compose -f docker-compose.prod.yml restart
```

**Check Resource Usage:**
```bash
# Container stats
docker stats

# Disk usage
df -h

# Database size
docker exec pokerstats-postgres-prod psql -U pokerstats -d pokerstats \
  -c "SELECT pg_size_pretty(pg_database_size('pokerstats'));"
```

### Automated Backups

Setup cron job for daily backups:

```bash
# Edit crontab
crontab -e

# Add daily backup at 2 AM
0 2 * * * cd /opt/pokerstats && bash scripts/backup-database.sh

# Add weekly data integrity check
0 3 * * 0 cd /opt/pokerstats && bash scripts/verify-data-integrity.sh
```

### Security Updates

```bash
# Update Docker images weekly
docker-compose -f docker-compose.prod.yml pull

# Update system packages
sudo apt update && sudo apt upgrade -y

# Restart if kernel updated
sudo reboot
```

---

## Troubleshooting

### Service Won't Start

```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs backend

# Check if port is in use
sudo lsof -i :8080

# Restart Docker
sudo systemctl restart docker
```

### Database Connection Issues

```bash
# Check PostgreSQL logs
docker-compose -f docker-compose.prod.yml logs postgres

# Test connection
docker exec pokerstats-postgres-prod pg_isready -U pokerstats

# Restart database
docker-compose -f docker-compose.prod.yml restart postgres
```

### High Memory Usage

```bash
# Check memory usage
free -h

# Restart services to free memory
docker-compose -f docker-compose.prod.yml restart

# Adjust JVM memory in docker-compose.prod.yml:
# JAVA_OPTS: "-Xms256m -Xmx512m"
```

### SSL Certificate Issues

```bash
# Check certificate expiry
openssl x509 -in nginx/ssl/fullchain.pem -noout -dates

# Renew Let's Encrypt certificate
sudo certbot renew

# Copy renewed certificates
sudo cp /etc/letsencrypt/live/pokerstats.yourdomain.com/*.pem nginx/ssl/
docker-compose -f docker-compose.prod.yml restart nginx
```

### Disk Space Issues

```bash
# Check disk usage
df -h

# Clean Docker resources
docker system prune -a

# Remove old backups
bash scripts/backup-database.sh  # Uses rotation policy

# Check large files
du -sh /mnt/pokerstats-data/*
```

---

## Performance Optimization

### For Raspberry Pi

```bash
# Increase swap (if RAM < 4GB)
sudo dphys-swapfile swapoff
sudo nano /etc/dphys-swapfile  # Set CONF_SWAPSIZE=2048
sudo dphys-swapfile setup
sudo dphys-swapfile swapon

# Reduce Docker logging
# Already configured in docker-compose.prod.yml

# Use overlay2 storage driver
docker info | grep "Storage Driver"
```

### Database Optimization

```bash
# Run VACUUM regularly
docker exec pokerstats-postgres-prod psql -U pokerstats -d pokerstats -c "VACUUM ANALYZE;"

# Check query performance
docker exec pokerstats-postgres-prod psql -U pokerstats -d pokerstats \
  -c "SELECT query, calls, total_time, mean_time FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"
```

---

## Disaster Recovery

### Full Backup

```bash
# Backup everything
tar -czf pokerstats-full-backup-$(date +%Y%m%d).tar.gz \
  /opt/pokerstats \
  /mnt/pokerstats-data/backups

# Store backup offsite
rsync -avz pokerstats-full-backup-*.tar.gz user@backup-server:/backups/
```

### Full Restore

```bash
# Extract backup
tar -xzf pokerstats-full-backup-YYYYMMDD.tar.gz -C /

# Restore database
bash scripts/restore-database.sh

# Start services
bash scripts/deploy.sh
```

---

## Support and Contact

For issues not covered in this guide:
1. Check the [Troubleshooting Guide](docs/TROUBLESHOOTING.md)
2. Review logs: `docker-compose -f docker-compose.prod.yml logs`
3. Check GitHub issues
4. Contact support: admin@pokerstats.local

---

**Last Updated**: October 2025  
**Version**: 1.0.0
