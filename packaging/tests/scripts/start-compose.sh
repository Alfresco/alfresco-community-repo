#!/usr/bin/env bash
set -x

export DOCKER_COMPOSE_PATH=$1
export DOCKER_COMPOSES=""
export CLEAN_UP=""

for var in "$@"
do
  if [ "$var" == "no-clean-up" ]
  then
    export CLEAN_UP="$var"
  else
    export DOCKER_COMPOSES+="--file $var "
  fi
done

if [ -z "$DOCKER_COMPOSES" ]
then
  echo "Please provide path to docker-compose.yml: \"${0##*/} /path/to/docker-compose.yml\""
  exit 1
fi

# Cleans up any generated images. These are created if the docker-compose file has "build:" clauses. They are not
# recreated if an image with the same name already exist. Also cleans up existing containers. Generally only needed on
# dev systems, however...
# The second parameter can be used to avoid doing a clean up if we are doing a restart test.
if [ "$CLEAN_UP" != "no-clean-up" ]
then
  docker compose ${DOCKER_COMPOSES} --project-directory $(dirname "${DOCKER_COMPOSE_PATH}") kill
  docker compose ${DOCKER_COMPOSES} --project-directory $(dirname "${DOCKER_COMPOSE_PATH}") rm -f

  export GENERATED_IMAGES=$(docker images | grep '^environment_' | awk '{ print $3 }')
  if [ -n "$GENERATED_IMAGES" ]
  then
    docker image rm -f $GENERATED_IMAGES
  fi
fi

echo "Starting ACS stack in ${DOCKER_COMPOSE_PATH}"

# If variables are not set from outside, get them from Maven
if [ -z "$TRANSFORMERS_TAG" ]; then
  export TRANSFORMERS_TAG=$(mvn help:evaluate -Dexpression=dependency.alfresco-transform-core.version -q -DforceStdout)
fi

if [ -z "$TRANSFORM_ROUTER_TAG" ]; then
  export TRANSFORM_ROUTER_TAG=$(mvn help:evaluate -Dexpression=dependency.alfresco-transform-service.version -q -DforceStdout)
fi

docker compose ${DOCKER_COMPOSES} --project-directory $(dirname "${DOCKER_COMPOSE_PATH}") up -d

if [ $? -eq 0 ]
then
  echo "Docker Compose started ok"
else
  echo "Docker Compose failed to start" >&2
  docker compose ${DOCKER_COMPOSES} logs --tail 200
  exit 1
fi
