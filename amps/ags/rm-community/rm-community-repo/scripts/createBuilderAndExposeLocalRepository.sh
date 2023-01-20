#!/usr/bin/env bash
docker run -d --restart=always --network=host --name registry registry:2
docker tag alfresco/alfresco-community-repo-base:latest localhost:5000/alfresco/alfresco-community-repo-base:latest
docker push localhost:5000/alfresco/alfresco-community-repo-base:latest
docker --config target/docker/alfresco/alfresco-governance-repository-community-base/latest/docker \
buildx create --use --name insecure-builder --driver-opt network=host \
--buildkitd-flags '--allow-insecure-entitlement security.insecure --allow-insecure-entitlement network.host'