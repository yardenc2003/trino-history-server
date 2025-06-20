#!/bin/bash
set -eu

VERSION=${1:-latest}
ARCH=${2:-amd64}
IMAGE_NAME="trino-history-backend:${VERSION}-${ARCH}"

# Paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}/.."

# 🔨 Build the Spring Boot project (run from root directory)
echo "🔧 Building project..."
(cd "$PROJECT_ROOT" && ./mvnw clean package -DskipTests)

# 🐳 Build the Docker image
echo "📦 Building Docker image for version: $VERSION and architecture: $ARCH..."
    docker build \
        "${PROJECT_ROOT}" \
        --progress=plain \
        --pull \
        --build-arg ARCH="${ARCH}" \
        --platform "linux/${ARCH}" \
        -f ./docker/Dockerfile \
        -t "${IMAGE_NAME}"

echo "✅ Built image: $IMAGE_NAME"
