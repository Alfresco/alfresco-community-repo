#!/usr/bin/env bash
echo "=========================== Starting Build Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

source "$(dirname "${BASH_SOURCE[0]}")/build_functions.sh"

if isBranchBuild && [ "${TRAVIS_BRANCH}" = "master" ] && [ "${TRAVIS_BUILD_STAGE_NAME,,}" = "release" ] ; then
  # update ":latest" image tags on remote repositories by using the maven *publish-docker-images* profile
  PROFILES="-Ppublish-docker-images"
else
  # build the ":latest" image tags locally with the maven *build-docker-images* profile
  PROFILES="-Pbuild-docker-images"
fi

# Build the current project
mvn -B -V install -DskipTests -Dmaven.javadoc.skip=true "${PROFILES}" -Pags


popd
set +vex
echo "=========================== Finishing Build Script =========================="

