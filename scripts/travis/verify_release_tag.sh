#!/usr/bin/env bash
echo "=========================== Starting Verify Release Tag Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

#
# Check that the version to be released does not already have a docker tag.
#

POM_VERSION=$(mvn -B -q help:evaluate -Dexpression=project.version -DforceStdout)
printf "POM version: %s\n" "${POM_VERSION}"

IMAGE_TAG="${POM_VERSION%-SNAPSHOT}"

if git rev-parse "${IMAGE_TAG}^{tag}" &>/dev/null ; then
  echo "The next tag \"${IMAGE_TAG}\" already exists in the git project"
  exit 1
fi

# get the image name from the pom file
ALFRESCO_DOCKER_IMAGE="$(mvn -B -q help:evaluate -f ./packaging/docker-alfresco/pom.xml -Dexpression=image.name -DforceStdout)"
DOCKER_IMAGE_FULL_NAME="${ALFRESCO_DOCKER_IMAGE}:${IMAGE_TAG}"

function docker_image_exists() {
  local IMAGE_FULL_NAME="${1}"; shift
  local WAIT_TIME="${1:-5}"
  local SEARCH_TERM='Pulling|is up to date|not found'

  echo "Looking to see if ${IMAGE_FULL_NAME} already exists..."
  local RESULT=$( (timeout --preserve-status "${WAIT_TIME}" docker 2>&1 pull "${IMAGE_FULL_NAME}" &) | grep -v 'Pulling repository' | grep -E -o "${SEARCH_TERM}")

  test "${RESULT}" || { echo "Timed out too soon. Try using a wait_time greater than ${WAIT_TIME}..."; return 1 ;}
  if echo "${RESULT}" | grep -vq 'not found'; then
      true
  else
      false
  fi
}

if docker_image_exists "${DOCKER_IMAGE_FULL_NAME}" ; then
    echo "Tag ${RELEASE_VERSION} already pushed, release process will interrupt."
    exit 1
else
    echo "The ${RELEASE_VERSION} tag was not found"
fi


popd
set +vex
echo "=========================== Finishing Verify Release Tag Script =========================="

