#!/usr/bin/env bash

BUILDER_NAME="${1}"
TARGET_REGISTRY="${2}"
TARGET_IMAGE="${3}"
IMAGE_TAG="${4}"

#Create a `docker-container` builder with host networking and required flags (quay.io)
docker --config target/docker/"${TARGET_REGISTRY}"/"${TARGET_IMAGE}"/"${IMAGE_TAG}"/docker \
buildx create --use --name "${BUILDER_NAME}" --driver-opt network=host \
--buildkitd-flags '--allow-insecure-entitlement security.insecure --allow-insecure-entitlement network.host'

#Create a `docker-container` builder with host networking and required flags (docker.io)
docker --config target/docker/"${TARGET_IMAGE}"/"${IMAGE_TAG}"/docker \
buildx create --use --name "${BUILDER_NAME}" --driver-opt network=host \
--buildkitd-flags '--allow-insecure-entitlement security.insecure --allow-insecure-entitlement network.host'

#Create a `docker-container` builder with host networking and required flags (local registry)
docker --config target/docker/127.0.0.1/5000/"${TARGET_IMAGE}"/"${IMAGE_TAG}"/docker \
buildx create --use --name "${BUILDER_NAME}" --driver-opt network=host \
--buildkitd-flags '--allow-insecure-entitlement security.insecure --allow-insecure-entitlement network.host'