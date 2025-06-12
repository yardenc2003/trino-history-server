#!/bin/bash
set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <version> [arch]"
  exit 1
fi

VERSION=${1:-1.0.0}
ARCH=${2:-amd64}
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
        -f Dockerfile \
        -t "${IMAGE_NAME}"

echo "✅ Built image: $IMAGE_NAME"
