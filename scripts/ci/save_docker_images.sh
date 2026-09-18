#!/usr/bin/env bash
#
# Saves the locally built ACS images so that the jobs which need a running
# Alfresco can load them instead of rebuilding the WAR and the image.
#
# Counterpart: load_docker_images.sh
#
echo "====================== Starting Save Docker Images ======================"
set -eo pipefail

OUT="${DOCKER_IMAGES_DIR:-/tmp/docker-images}"
mkdir -p "${OUT}"

# Both images are produced by a single "-Pags,build-docker-images" build; the
# governance image is layered on top of the community one, so saving them
# together stores the shared layers once.
IMAGES=(
  "alfresco/alfresco-community-repo-base:latest"
  "alfresco/alfresco-governance-repository-community-base:latest"
)

for image in "${IMAGES[@]}"; do
  docker image ls "${image}"
done
docker save "${IMAGES[@]}" | zstd -3 -T0 -o "${OUT}/acs-images.tzst"

ls -lh "${OUT}"
echo "===================== Finishing Save Docker Images ======================"
