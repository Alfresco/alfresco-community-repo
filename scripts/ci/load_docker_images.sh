#!/usr/bin/env bash
#
# Loads the ACS images built once by the "Build Docker images" job.
#
# Counterpart: save_docker_images.sh
#
echo "====================== Starting Load Docker Images ======================"
set -eo pipefail

IN="${DOCKER_IMAGES_DIR:-/tmp/docker-images}"

zstd -d -T0 -c "${IN}/acs-images.tzst" | docker load

docker image ls "alfresco/alfresco-community-repo-base"
docker image ls "alfresco/alfresco-governance-repository-community-base"

echo "===================== Finishing Load Docker Images ======================"
