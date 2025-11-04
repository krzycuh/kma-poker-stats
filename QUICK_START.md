# Poker Stats - Quick Start Guide

## 🚀 Production Deployment Quick Reference

This guide provides quick commands for deploying and managing Poker Stats in production.

---

## Prerequisites

- Linux server (Raspberry Pi 4 recommended, 4GB+ RAM)
- Docker and Docker Compose installed
- External SSD mounted (recommended)
- Domain name with SSL certificate

---

## Initial Setup (First Time Only)

### 1. Clone and Configure

```bash
# Clone repository
git clone <your-repo-url> /opt/pokerstats
cd /opt/pokerstats

# Copy and configure environment
cp .env.production.template .env.production
nano .env.production  # Edit all CHANGE_ME values

# Generate secure secrets
echo "POSTGRES_PASSWORD=$(openssl rand -base64 32)" >> .env.production
echo "REDIS_PASSWORD=$(openssl rand -base64 32)" >> .env.production
echo "JWT_SECRET=$(openssl rand -base64 64)" >> .env.production
echo "GRAFANA_ADMIN_PASSWORD=$(openssl rand -base64 20)" >> .env.production
```

### 2. Setup SSL (Let's Encrypt)

```bash
# Install certbot
sudo apt install certbot

# Generate certificate
sudo certbot certonly --standalone -d pokerstats.yourdomain.com

# Copy certificates
sudo mkdir -p nginx/ssl
sudo cp /etc/letsencrypt/live/pokerstats.yourdomain.com/fullchain.pem nginx/ssl/
sudo cp /etc/letsencrypt/live/pokerstats.yourdomain.com/privkey.pem nginx/ssl/
sudo cp /etc/letsencrypt/live/pokerstats.yourdomain.com/chain.pem nginx/ssl/
```

### 3. Deploy

```bash
# First deployment
bash scripts/deploy.sh
```

### 4. Create Admin User

```bash
# Create first admin account
bash scripts/create-admin-user.sh
```

### 5. (Optional) Add Demo Data

```bash
# For testing purposes
bash scripts/seed-demo-data.sh
```

---

## Daily Operations

### Deploy New Version

```bash
cd /opt/pokerstats
bash scripts/deploy.sh
```

### View Logs

```bash
# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker-compose -f docker-compose.prod.yml logs -f backend
docker-compose -f docker-compose.prod.yml logs -f postgres
```

### Restart Services

```bash
# Restart all
docker-compose -f docker-compose.prod.yml restart

# Restart specific service
docker-compose -f docker-compose.prod.yml restart backend
```

### Check Status

```bash
# Container status
docker-compose -f docker-compose.prod.yml ps

# Health check
curl http://localhost:8080/actuator/health

# Resource usage
docker stats
```

---

## Backup and Restore

### Manual Backup

```bash
# Create backup
bash scripts/backup-database.sh
```

### Restore from Backup

```bash
# Interactive restore
bash scripts/restore-database.sh
```

### Automated Backups (Setup Cron)

```bash
# Edit crontab
crontab -e

# Add daily backup at 2 AM
0 2 * * * cd /opt/pokerstats && bash scripts/backup-database.sh

# Add weekly integrity check
0 3 * * 0 cd /opt/pokerstats && bash scripts/verify-data-integrity.sh
```

---

## Troubleshooting

### Service Won't Start

```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs backend

# Restart Docker
sudo systemctl restart docker

# Rebuild and restart
docker-compose -f docker-compose.prod.yml up -d --build
```

### Database Issues

```bash
# Check database health
docker exec pokerstats-postgres-prod pg_isready -U pokerstats

# Restart database
docker-compose -f docker-compose.prod.yml restart postgres

# Run integrity check
bash scripts/verify-data-integrity.sh
```

### Performance Issues

```bash
# Check resource usage
free -h
df -h
docker stats

# Restart services to free memory
docker-compose -f docker-compose.prod.yml restart
```

### Rollback

```bash
# Interactive rollback
bash scripts/rollback.sh

# Select rollback method and follow prompts
```

---

## Monitoring

### Monitoring Endpoints

- **Application**: https://pokerstats.yourdomain.com
- **Health**: https://pokerstats.yourdomain.com/api/actuator/health
- **Metrics**: https://pokerstats.yourdomain.com/api/actuator/metrics

### Check Metrics

```bash
# Application metrics (local)
curl http://localhost:8080/actuator/metrics
```

---

## Maintenance

### Daily

```bash
# Check logs for errors
docker-compose -f docker-compose.prod.yml logs | grep ERROR

# Monitor disk space
df -h
```

### Weekly

```bash
# Verify backups exist
ls -lh /mnt/pokerstats-data/backups/postgres/

# Run data integrity check
bash scripts/verify-data-integrity.sh
```

### Monthly

```bash
# Update system packages
sudo apt update && sudo apt upgrade

# Renew SSL certificate (automatic, verify)
sudo certbot renew

# Check Docker disk usage
docker system df
docker system prune  # Clean up if needed
```

---

## Common Commands Cheat Sheet

```bash
# View all containers
docker ps

# Stop all services
docker-compose -f docker-compose.prod.yml down

# Start all services
docker-compose -f docker-compose.prod.yml up -d

# Follow logs in real-time
docker-compose -f docker-compose.prod.yml logs -f

# Execute command in container
docker exec -it pokerstats-backend-prod /bin/bash

# Database shell
docker exec -it pokerstats-postgres-prod psql -U pokerstats -d pokerstats

# Redis shell
docker exec -it pokerstats-redis-prod redis-cli --pass $REDIS_PASSWORD

# Check container health
docker inspect pokerstats-backend-prod --format='{{.State.Health.Status}}'

# Restart single service without downtime
docker-compose -f docker-compose.prod.yml up -d --no-deps --build backend
```

---

## Scripts Reference

| Script | Purpose |
|--------|---------|
| `scripts/deploy.sh` | Deploy application to production |
| `scripts/rollback.sh` | Rollback to previous version |
| `scripts/backup-database.sh` | Create database backup |
| `scripts/restore-database.sh` | Restore from backup |
| `scripts/create-admin-user.sh` | Create admin account |
| `scripts/seed-demo-data.sh` | Add demo data |
| `scripts/verify-data-integrity.sh` | Check database integrity |

---

## Documentation

- **Deployment Guide**: [DEPLOYMENT.md](DEPLOYMENT.md)
- **User Onboarding**: [docs/USER_ONBOARDING.md](docs/USER_ONBOARDING.md)
- **Admin Guide**: [docs/ADMIN_GUIDE.md](docs/ADMIN_GUIDE.md)
- **FAQ**: [docs/FAQ.md](docs/FAQ.md)
- **Troubleshooting**: [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)
- **Feedback**: [docs/FEEDBACK.md](docs/FEEDBACK.md)
- **Phase 8 Summary**: [PHASE_8_SUMMARY.md](PHASE_8_SUMMARY.md)

---

## Environment Variables

Key variables in `.env.production`:

```bash
# Database
POSTGRES_PASSWORD=<generated>
POSTGRES_DB=pokerstats
POSTGRES_USER=pokerstats

# Redis
REDIS_PASSWORD=<generated>

# JWT
JWT_SECRET=<generated>
JWT_EXPIRATION=86400000  # 24 hours

# Storage
DATA_PATH=/mnt/pokerstats-data

# Domain
CORS_ALLOWED_ORIGINS=https://pokerstats.yourdomain.com
VITE_API_URL=https://pokerstats.yourdomain.com/api

# Monitoring
GRAFANA_ADMIN_PASSWORD=<generated>
```

---

## Support

**Issues?** Check:
1. [Troubleshooting Guide](docs/TROUBLESHOOTING.md)
2. Logs: `docker-compose -f docker-compose.prod.yml logs`
3. GitHub Issues

**Questions?**
- Email: support@pokerstats.local
- Documentation: [docs/](docs/)

---

**Version**: 1.0.0  
**Last Updated**: October 2025

**Happy Deploying! 🎰🚀**
