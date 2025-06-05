#!/usr/bin/env bash
echo "=========================== Starting Build Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

source "$(dirname "${BASH_SOURCE[0]}")/build_functions.sh"

GIT_REPO="github.com/Alfresco/alfresco-transform-core.git"
BRANCH="MNT-24883-libreoffice-header-fix"

buildCoreAio "${GIT_REPO}" "${BRANCH}"

if [[ -n ${BUILD_PROFILES} ]]; then
  PROFILES="${BUILD_PROFILES}"
elif [[ "${REQUIRES_LOCAL_IMAGES}" == "true" ]]; then
  PROFILES="-Pbuild-docker-images -Pags"
else
  PROFILES="-Pags"
fi

if [[ "${REQUIRES_INSTALLED_ARTIFACTS}" == "true" ]]; then
  PHASE="install"
else
  PHASE="package"
fi

mvn -B -V $PHASE -DskipTests -Dmaven.javadoc.skip=true $PROFILES $BUILD_OPTIONS

popd
set +vex
echo "=========================== Finishing Build Script =========================="

