#!/usr/bin/env bash

export DOCKER_COMPOSE_PATH="${1}"

if [ -z "$DOCKER_COMPOSE_PATH" ] ; then
  echo "Please provide path to docker-compose.yml: \"${0##*/} /path/to/docker-compose.yml\""
  exit 1
fi

# Fix uppercase bucket name if set
if [ -n "${S3_BUCKET_NAME}" ] ; then
  export S3_BUCKET_NAME="${S3_BUCKET_NAME,,}"
fi

echo "Starting AGS stack in ${DOCKER_COMPOSE_PATH}"

# .env files are picked up from project directory correctly on docker-compose 1.23.0+
docker-compose --file "${DOCKER_COMPOSE_PATH}" --project-directory $(dirname "${DOCKER_COMPOSE_PATH}") up -d

if [ $? -eq 0 ] ; then
  echo "Docker Compose started ok"
else
  echo "Docker Compose failed to start" >&2
  exit 1
fi

WAIT_INTERVAL=1
COUNTER=0
TIMEOUT=300
t0=$(date +%s)

echo "Waiting for alfresco to start"
until $(curl --output /dev/null --silent --head --fail http://localhost:8080/alfresco) || [ "$COUNTER" -eq "$TIMEOUT" ]; do
   printf '.'
   sleep ${WAIT_INTERVAL}
   COUNTER=$(($COUNTER+$WAIT_INTERVAL))
done

if (("$COUNTER" < "$TIMEOUT")) ; then
   t1=$(date +%s)
   delta=$(( ($t1 - $t0)/60 ))
   echo "Alfresco Started in $delta minutes"
else
   echo "Waited ${COUNTER} seconds"
   echo "Alfresco Could not start in time."
   exit 1
fi
