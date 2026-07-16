#!/bin/sh
set -e
IMAGE="${IMAGE:-cicd-demo:latest}"
NAME="${NAME:-cicd-demo}"
PORT="${PORT:-18080}"
docker rm -f "$NAME" 2>/dev/null || true
docker run -d --name "$NAME" -p "${PORT}:8080" -e APP_VERSION="${APP_VERSION:-1.0.0}" "$IMAGE"
echo "deployed $NAME on :$PORT"
