#!/usr/bin/env bash
set -x

#stop not needed containers
docker stop $(docker ps -a | grep '_zeppelin_' | awk '{print $1}')
docker stop $(docker ps -a | grep '_sync-service_' | awk '{print $1}')

shareContainerId=$(docker ps -a | grep '_share_' | awk '{print $1}')
if [ -n "$shareContainerId" ]; then
      docker stop $(docker ps -a | grep '_transform-router_' | awk '{print $1}')
      docker stop $(docker ps -a | grep '_shared-file-store_' | awk '{print $1}')
fi

# Display containers resources usage
docker stats --no-stream
