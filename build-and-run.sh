#!/bin/bash
set -eu

# Config
APP_NAME="file-manager-server"
CONTAINER_NAME="${APP_NAME}-container"

# Go to project directory
cd /home/ubuntu/$APP_NAME || { echo "Project directory not found"; exit 1; }

# Stop & remove previous container if it exists
echo "Cleaning old container if exists..."
docker-compose down

# Build & Run
echo "Building and starting Docker image..."
docker-compose up -d --build

echo "Container $CONTAINER_NAME is now running at port 8080"
