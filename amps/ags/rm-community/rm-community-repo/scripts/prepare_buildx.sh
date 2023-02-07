#!/usr/bin/env bash

LOCAL_REGISTRY="${1}"
BASE_IMAGE="${2}"
BASE_IMAGE_TAG="${3}"
BUILDER_NAME="${4}"
TARGET_REGISTRY="${5}"
TARGET_IMAGE="${6}"
TARGET_TAG="${7}"
SLEEP_SECONDS=0

if [ "${LOCAL_REGISTRY}" != "127.0.0.1:5000" ]; then
  echo "The local registry is not set to 127.0.0.1:5000. Skipping image push."
else
  #Run a local registry server
  docker run -d -p 5000:5000 --restart=always --name registry registry:2

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
fi