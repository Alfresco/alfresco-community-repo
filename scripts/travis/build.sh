#!/usr/bin/env bash
echo "=========================== Starting Build Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

source "$(dirname "${BASH_SOURCE[0]}")/build_functions.sh"


# Build the current project if needed
if [[ -n ${REQUIRES_INSTALLED_ARTIFACTS} ]] || [[ -n ${REQUIRES_LOCAL_IMAGES} ]] || [[ -n ${BUILD_PROFILES} ]]; then

  if [[ -n ${BUILD_PROFILES} ]]; then
    PROFILES="${BUILD_PROFILES}"
  else
    if [[ "${REQUIRES_LOCAL_IMAGES}" == "true" ]]; then
      PROFILES="-Pbuild-docker-images -Pags"
    else
      PROFILES="-Pags"
    fi
  fi

  if [[ "${REQUIRES_INSTALLED_ARTIFACTS}" == "true" ]]; then
    PHASE="install"
  else
    PHASE="package"
  fi

  mvn -B -V $PHASE -DskipTests -Dmaven.javadoc.skip=true $PROFILES $BUILD_OPTIONS
fi


popd
set +vex
echo "=========================== Finishing Build Script =========================="

