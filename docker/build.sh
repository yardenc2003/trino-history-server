#!/bin/bash
set -e

VERSION=${1:-latest}
ARCH=${2:-arm64}
IMAGE_NAME="trino-history-backend:${VERSION}-${ARCH}"

# Paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}/.."

# Build the Docker image
    docker build \
        "${PROJECT_ROOT}" \
        --progress=plain \
        --pull \
        --build-arg ARCH="${ARCH}" \
        --platform "linux/${ARCH}" \
        -f ./docker/Dockerfile \
        -t "${IMAGE_NAME}"

echo "✅ Built image: $IMAGE_NAME"
