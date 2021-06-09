#!/usr/bin/env bash
set -x

# Display running containers
docker ps

alfrescoContainerId=$(docker ps -a | grep '_alfresco_' | awk '{print $1}')
shareContainerId=$(docker ps -a | grep '_share_' | awk '{print $1}')
solrContainerId=$(docker ps -a | grep '_search_' | awk '{print $1}')

docker logs $alfrescoContainerId > alfresco.log
if [ -n "$shareContainerId" ]; then
  docker logs $shareContainerId > share.log
fi
if [ -n "$solrContainerId" ]; then
  docker logs $solrContainerId > solr.log
fi
