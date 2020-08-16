#!/usr/bin/env bash

export DOCKER_COMPOSE_PATH=$1

if [ -z "$DOCKER_COMPOSE_PATH" ]
then
  echo "Please provide path to docker-compose.yml: \"${0##*/} /path/to/docker-compose.yml\""
  exit 1
fi

echo "Killing ACS stack in ${DOCKER_COMPOSE_PATH}"

docker-compose --file ${DOCKER_COMPOSE_PATH} ps
# logs for debug
docker-compose --file ${DOCKER_COMPOSE_PATH} logs --no-color -t alfresco
docker-compose --file ${DOCKER_COMPOSE_PATH} kill
docker-compose --file ${DOCKER_COMPOSE_PATH} rm -fv