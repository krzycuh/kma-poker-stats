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