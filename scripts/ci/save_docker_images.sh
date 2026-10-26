#!/usr/bin/env bash
#
# Saves the locally built ACS images, so that the jobs needing a running Alfresco can load them instead of
# building the images themselves.
#
# Counterpart: load_docker_images.sh
#
echo "========================== Starting Save Docker Images ==========================="
set -eo pipefail

OUT="${DOCKER_IMAGES_DIR:-/tmp/docker-images}"
mkdir -p "${OUT}"

# The governance image is layered on the community one, so saving both together stores the shared layers once.
IMAGES=(
  "alfresco/alfresco-community-repo-base:latest"
  "alfresco/alfresco-governance-repository-community-base:latest"
)

docker image inspect "${IMAGES[@]}" > /dev/null
docker save "${IMAGES[@]}" | zstd -3 -T0 -o "${OUT}/acs-images.tar.zst"

ls -lh "${OUT}"
echo "========================== Finishing Save Docker Images =========================="
