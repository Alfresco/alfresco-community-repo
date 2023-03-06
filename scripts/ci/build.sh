#!/usr/bin/env bash
echo "=========================== Starting Build Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

source "$(dirname "${BASH_SOURCE[0]}")/build_functions.sh"

# TODO
#if isBranchBuild && [ "${BRANCH_NAME}" = "master" ] && [ "${BUILD_STAGE_NAME,,}" = "release" ] ; then
  # update ":latest" image tags on remote repositories by using the maven *internal* profile
#  PROFILES="-Pinternal"

if [[ -n ${BUILD_PROFILES} ]]; then
  PROFILES="${BUILD_PROFILES}"
elif [[ "${REQUIRES_LOCAL_IMAGES}" == "true" ]]; then
  # build the ":latest" image tags locally with the maven *communityDocker* profile
  PROFILES="-PcommunityDocker"
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