# 🔧 How to prepare production deployment of a new application version?

## Step 1: Build new images with fixes

### Build Backend
`docker build -t ghcr.io/$GITHUB_USER/kma-poker-stats/backend:main ./backend`

### Build Frontend
`docker build -t ghcr.io/$GITHUB_USER/kma-poker-stats/frontend:main ./frontend`

## Step 2: Push to GitHub Container Registry

### Login to GitHub Container Registry
`echo $GITHUB_TOKEN | docker login ghcr.io -u $GITHUB_USER --password-stdin`

### Push images
```
docker push ghcr.io/$GITHUB_USER/kma-poker-stats/backend:main
docker push ghcr.io/$GITHUB_USER/kma-poker-stats/frontend:main
```
## Database backups (prod)

`scripts/backup-database.sh` writes to `$BACKUP_PATH/postgres` (from `.env.production`,
default `/mnt/pokerstats-data/backups`). It can be run from any directory - the
`.env.production` file is always read from the repository root.

### One-time storage setup

`/mnt/...` is owned by `root`, so the directory has to be created and handed over
to the user that runs the backups (do this after the SSD is mounted):

```
sudo mkdir -p /mnt/pokerstats-data/backups/postgres
sudo chown -R $USER:$USER /mnt/pokerstats-data
```

If you do not use an external disk, point the backups at a directory you already
own by setting `BACKUP_PATH` in `.env.production`:

```
BACKUP_PATH=/home/krzychu/pokerstats-data/backups
```

### Running

```
./scripts/backup-database.sh      # create a backup + rotate old ones
./scripts/restore-database.sh     # pick a backup and restore it
```
