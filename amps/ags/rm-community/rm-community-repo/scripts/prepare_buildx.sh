#!/usr/bin/env bash

LOCAL_REGISTRY_HOST="${1}"
LOCAL_REGISTRY_PORT="${2}"
BASE_IMAGE="${3}"
BASE_IMAGE_TAG="${4}"
BUILDER_NAME="${5}"
TARGET_REGISTRY="${6}"
TARGET_IMAGE="${7}"
TARGET_TAG="${8}"
LOCAL_REGISTRY="${LOCAL_REGISTRY_HOST}":"${LOCAL_REGISTRY_PORT}"
SLEEP_SECONDS=0

#Run a local registry server
docker run -d -p "${LOCAL_REGISTRY_PORT}":"${LOCAL_REGISTRY_PORT}" --restart=always --name registry registry:2

#Push base image to the local repository
docker tag "${BASE_IMAGE}":"${BASE_IMAGE_TAG}" "${LOCAL_REGISTRY}"/"${BASE_IMAGE}":"${BASE_IMAGE_TAG}"
while [ "$( docker container inspect -f '{{.State.Running}}' registry )" != "true" ] && [ $SLEEP_SECONDS -lt 600 ]
do
  ((SLEEP_SECONDS++))
  sleep 1
done
docker push "${LOCAL_REGISTRY}"/"${BASE_IMAGE}":"${BASE_IMAGE_TAG}"

#Create a `docker-container` builder with host networking and required flags (quay.io)
docker --config target/docker/"${TARGET_REGISTRY}"/"${TARGET_IMAGE}"/"${TARGET_TAG}"/docker \
buildx create --use --name "${BUILDER_NAME}" --driver-opt network=host \
--buildkitd-flags '--allow-insecure-entitlement security.insecure --allow-insecure-entitlement network.host'

#Create a `docker-container` builder with host networking and required flags (docker.io)
docker --config target/docker/"${TARGET_IMAGE}"/"${TARGET_TAG}"/docker \
buildx create --use --name "${BUILDER_NAME}" --driver-opt network=host \
--buildkitd-flags '--allow-insecure-entitlement security.insecure --allow-insecure-entitlement network.host'