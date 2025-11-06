# Poker Stats – Production Deployment Guide

This guide explains how to deploy and operate Poker Stats in production after migrating to GitHub Container Registry (GHCR). The CI pipeline now builds the frontend and backend images and publishes floating tags (for example `:main`). Production updates are executed manually on the Raspberry Pi by pulling those images and restarting the stack with Docker Compose.

---

## Quick Reference

### Update to the latest `main` build

```bash
ssh <user>@<rpi-host>
cd /opt/pokerstats

# Ensure GHCR credentials are configured (login steps below)
docker compose --env-file .env.production -f docker-compose.prod.yml pull backend frontend
docker compose --env-file .env.production -f docker-compose.prod.yml up -d

# Verify
docker compose --env-file .env.production -f docker-compose.prod.yml ps
curl -f https://pokerstats.yourdomain.com/api/actuator/health
```

### Roll back to a specific tag

```bash
cd /opt/pokerstats
VERSION=2025.10.31 docker compose --env-file .env.production -f docker-compose.prod.yml pull backend frontend
VERSION=2025.10.31 docker compose --env-file .env.production -f docker-compose.prod.yml up -d
```

### Manual database backup

```bash
cd /opt/pokerstats
bash scripts/backup-database.sh
```

---

## Architecture Overview

The production stack now contains the following long-running services:

- `postgres`: PostgreSQL 16 with data persisted under `DATA_PATH/postgres`
- `redis`: Redis cache with append-only persistence under `DATA_PATH/redis`
- `backend`: Spring Boot API served from `ghcr.io/krzycuh/kma-poker-stats/backend:<tag>`
- `frontend`: Static SPA + Nginx proxy from `ghcr.io/krzycuh/kma-poker-stats/frontend:<tag>`
- `nginx`: TLS termination and routing between the public internet, the frontend container, and the backend API

Prometheus and Grafana were removed from the default deployment. If you still need metrics, host them separately.

---

## One-Time Environment Preparation

### 1. Base system

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y git curl openssl ca-certificates

# Install Docker Engine + Compose plugin
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker
docker --version
docker compose version
```

### 2. Storage layout

```bash
sudo mkdir -p /mnt/pokerstats-data
sudo chown -R $USER:$USER /mnt/pokerstats-data

# Optional: persist SSD mount in /etc/fstab as needed

mkdir -p /mnt/pokerstats-data/{postgres,redis,backups}
mkdir -p /opt/pokerstats/logs/{backend,nginx}
```

### 3. Retrieve deployment files

```bash
sudo mkdir -p /opt/pokerstats
sudo chown -R $USER:$USER /opt/pokerstats
cd /opt/pokerstats
git clone https://github.com/krzycuh/kma-poker-stats.git .
git checkout main
```

> The repository is only required for the Compose definition, configuration files, and helper scripts. Application code is delivered exclusively through GHCR images.

### 4. Configure environment

Copy the template and populate secrets:

```bash
cp .env.production.template .env.production
nano .env.production
```

Recommended variables:

```
POSTGRES_DB=pokerstats
POSTGRES_USER=pokerstats
POSTGRES_PASSWORD=<generated>
REDIS_PASSWORD=<generated>
JWT_SECRET=<generated>
JWT_EXPIRATION=86400000
DATA_PATH=/mnt/pokerstats-data
CORS_ALLOWED_ORIGINS=https://pokerstats.yourdomain.com
VITE_API_URL=https://pokerstats.yourdomain.com/api
VERSION=main
```

Store the file securely; it is consumed by Docker Compose and helper scripts.

### 5. GHCR authentication

1. Generate a GitHub personal access token with the `read:packages` scope.
2. Log in once on the production host:

   ```bash
   echo <TOKEN> | docker login ghcr.io -u <github-username> --password-stdin
   ```

Docker caches the credentials; renew the token before it expires.

### 6. TLS certificates

Install Let’s Encrypt certificates (recommended):

```bash
sudo apt install certbot
sudo certbot certonly --standalone -d pokerstats.yourdomain.com

sudo mkdir -p /opt/pokerstats/nginx/ssl
sudo cp /etc/letsencrypt/live/pokerstats.yourdomain.com/{fullchain.pem,privkey.pem,chain.pem} /opt/pokerstats/nginx/ssl/
```

Set up automatic renewal via `certbot renew` and copy the updated files after each renewal. Reload the `nginx` container with `docker compose exec nginx nginx -s reload` when certificates change.

### 7. Firewall (UFW example)

```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
sudo ufw status
```

---

## Deploying the Stack

### 1. First boot

```bash
cd /opt/pokerstats
docker compose --env-file .env.production -f docker-compose.prod.yml pull
docker compose --env-file .env.production -f docker-compose.prod.yml up -d
```

The backend image runs Flyway migrations automatically on startup. Monitor logs while the initial migration completes:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml logs -f backend
```

### 2. Create the first admin account

```bash
cd /opt/pokerstats
bash scripts/create-admin-user.sh
```

Store the generated credentials securely.

### 3. (Optional) Seed demo data

```bash
bash scripts/seed-demo-data.sh
```

---

## Updating to a New Build

1. Confirm you are logged in to GHCR (`docker login ghcr.io`).
2. Pull the latest images (defaults to `VERSION=main`):

   ```bash
   docker compose --env-file .env.production -f docker-compose.prod.yml pull backend frontend
   ```

3. Apply the update:

   ```bash
   docker compose --env-file .env.production -f docker-compose.prod.yml up -d
   ```

4. Verify:

   ```bash
   docker compose --env-file .env.production -f docker-compose.prod.yml ps
   curl -f https://pokerstats.yourdomain.com/api/actuator/health
   ```

Downtime is limited to the container restart; data volumes remain intact.

---

## Rolling Back

1. List available tags:

   ```bash
   curl -s https://ghcr.io/v2/krzycuh/kma-poker-stats/backend/tags/list | jq '.tags'
   ```

2. Choose the target tag (for example `2025.10.31`).
3. Override `VERSION` and redeploy:

   ```bash
   VERSION=2025.10.31 docker compose --env-file .env.production -f docker-compose.prod.yml pull backend frontend
   VERSION=2025.10.31 docker compose --env-file .env.production -f docker-compose.prod.yml up -d
   ```

Record the tag you rolled back to so future upgrades can bump forward deliberately.

---

## Backups and Maintenance

- **Database backup:** `bash scripts/backup-database.sh`
- **Database restore:** `bash scripts/restore-database.sh`
- **Data integrity check:** `bash scripts/verify-data-integrity.sh`
- **Watch logs:** `docker compose --env-file .env.production -f docker-compose.prod.yml logs -f <service>`
- **Restart a service:** `docker compose --env-file .env.production -f docker-compose.prod.yml restart <service>`
- **Prune unused images:** `docker image prune -f`

Schedule backups via cron (example):

```bash
0 2 * * * cd /opt/pokerstats && bash scripts/backup-database.sh
0 3 * * 0 cd /opt/pokerstats && bash scripts/verify-data-integrity.sh
```

---

## Troubleshooting

| Symptom | Checks | Remedies |
| --- | --- | --- |
| `backend` container keeps restarting | `docker compose logs backend` | Verify database credentials, ensure Postgres is healthy (`docker compose logs postgres`) |
| Site returns 502/504 | `docker compose ps`, `docker compose logs nginx` | Pull latest images, restart `nginx`, verify TLS certificates |
| Image pulls fail | `docker logout ghcr.io` then re-run `docker login ghcr.io` | Renew GHCR token, confirm network access |
| Database connection refused | `docker compose exec postgres pg_isready -U $POSTGRES_USER` | Restart Postgres, restore from backup if corrupt |

Additional health checks:

```bash
# Disk space
df -h

# Memory pressure
free -h

# Container resource usage
docker stats
```

---

## Frequently Used Paths

- Compose file: `docker-compose.prod.yml`
- TLS certificates: `nginx/ssl/`
- Reverse proxy configuration: `nginx/nginx.prod.conf`
- Logs (host): `/opt/pokerstats/logs/{backend,nginx}`
- Persistent data: `${DATA_PATH}/postgres`, `${DATA_PATH}/redis`, `${DATA_PATH}/backups`

Keep these directories under restricted permissions and include them in your host-level backups.

---

## Change Log

- **November 2025:** Consolidated Quick Start content into this guide, switched production workflow to GHCR images, and removed Prometheus/Grafana references.

For additional operational guidance, consult `docs/TROUBLESHOOTING.md` and `docs/ADMIN_GUIDE.md`.
